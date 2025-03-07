package com.example.tradingproject

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
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
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody


class RegisterEmailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        BtnController()

    }


    private fun BtnController() {
        val btnbackToLogin = findViewById<TextView>(R.id.btnbackToLogin)
        val btnRegisterEmail = findViewById<Button>(R.id.btnRegisterEmail)
        btnbackToLogin.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        btnRegisterEmail.setOnClickListener() {
            val emailEdit = findViewById<EditText>(R.id.edit_email)

            val email = emailEdit.text.toString().trim()
            RegisterEmail(email)
        }

        btnbackToLogin.setOnClickListener() {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }

    private fun RegisterEmail(email: String) {
        val client = OkHttpClient()

        val url = getString(R.string.root_url) + getString(R.string.register_email)

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
                                this@RegisterEmailActivity,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@RegisterEmailActivity, RegisterEmailOTPActivity::class.java)
                            startActivity(intent)
                        } catch (jsonEx: Exception) {
                            // ถ้า parse JSON ไม่ได้ แสดง raw response
                            Toast.makeText(
                                this@RegisterEmailActivity,
                                "Registration success: $responseBody",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ไปหน้า OTP ตามเดิม
                            val intent = Intent(this@RegisterEmailActivity, RegisterEmailOTPActivity::class.java)
                            startActivity(intent)
                        }

                    } else {
                        try {
                            val jsonObject = org.json.JSONObject(responseBody)
                            val error = jsonObject.optString("error", "Registration failed")
                            Toast.makeText(
                                this@RegisterEmailActivity,
                                "$error (HTTP ${response.code})",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (jsonEx: Exception) {
                            // ถ้า parse JSON ไม่ได้ แสดง raw response
                            Toast.makeText(
                                this@RegisterEmailActivity,
                                "Error (HTTP ${response.code}): $responseBody",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterEmailActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    }

