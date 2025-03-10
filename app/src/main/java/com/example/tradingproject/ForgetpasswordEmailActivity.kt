package com.example.tradingproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ForgetpasswordEmailActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgetpassword_email)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailEditText = findViewById<EditText>(R.id.edit_email)
        val btnRegisterEmail = findViewById<Button>(R.id.btnRegisterEmail)
        val btnbackToLogin = findViewById<TextView>(R.id.btnbackToLogin)

        btnRegisterEmail.setOnClickListener {
            val emailEditText = emailEditText.text.toString()
            if (emailEditText.isNotEmpty()) {
                ForgetpasswordEmail(emailEditText)
            } else {
                Toast.makeText(this, "กรุณากรอกอีเมล", Toast.LENGTH_SHORT).show()
            }
        }
        btnbackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))

        }
    }

    private fun ForgetpasswordEmail(email: String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + "/api/forgot-password"
        val formBody: RequestBody = FormBody.Builder()
            .add("email", email)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "No response body"

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val jsonObject = org.json.JSONObject(responseBody)
                            val message = jsonObject.optString("message", "Registration success")

                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@ForgetpasswordEmailActivity, ForgetpasswordOTPActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } catch (jsonEx: Exception) {
                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                "Registration success: $responseBody",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@ForgetpasswordEmailActivity, ForgetpasswordOTPActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        }
                    } else {
                        try {
                            val jsonObject = org.json.JSONObject(responseBody)
                            val error = jsonObject.optString("error", "Registration failed")
                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                "$error (HTTP ${response.code})",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (jsonEx: Exception) {
                            // ถ้า parse JSON ไม่ได้ แสดง raw response
                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                "Error (HTTP ${response.code}): $responseBody",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ForgetpasswordEmailActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
