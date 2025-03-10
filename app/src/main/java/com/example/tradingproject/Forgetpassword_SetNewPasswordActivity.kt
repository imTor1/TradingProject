package com.example.tradingproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class Forgetpassword_SetNewPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgetpassword_set_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonConfirmpassword = findViewById<Button>(R.id.confirm_button_password)
        val email = intent.getStringExtra("email") ?: ""
        val Password = findViewById<EditText>(R.id.edit_password)
        val ConfirmPassword = findViewById<EditText>(R.id.edit_confirmpassword)
        buttonConfirmpassword.setOnClickListener {
            val set_password = Password.text.toString().trim()
            val con_password = ConfirmPassword.text.toString().trim()

            val btnbackToLogin: TextView = findViewById(R.id.btnbackToLogin)
            btnbackToLogin.setOnClickListener {
                startActivity(Intent(this, LoginPage::class.java))

            }

            if (set_password == con_password) {
                setForgetPassword(email,set_password)
            }else if (set_password.isEmpty() || con_password.isEmpty()) {
                Toast.makeText(this, "Please fill your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (con_password != con_password) {
                Toast.makeText(this, "Password not match !!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("SetPassword", "Setting password for email: $email,Password $set_password")
        }

    }
    private fun setForgetPassword(email: String, password: String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + getString(R.string.set_password)
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "No response"

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val message = jsonObject.optString("message", "OTP ถูกต้อง")
                            val token = jsonObject.optString("token", "")


                            Toast.makeText(
                                this@Forgetpassword_SetNewPasswordActivity,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()

                            if (token.isNotEmpty()) {
                                // เก็บ Token ไว้ใน SharedPreferences
                                val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    putString("TOKEN", token)
                                    apply()
                                }
                            }
                            val intent = Intent(this@Forgetpassword_SetNewPasswordActivity ,MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } catch (jsonEx: Exception) {
                            Toast.makeText(
                                this@Forgetpassword_SetNewPasswordActivity,
                                "Error parsing response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("error", "เกิดข้อผิดพลาด")
                        } catch (jsonEx: Exception) {
                            responseBody
                        }

                        Toast.makeText(
                            this@Forgetpassword_SetNewPasswordActivity,
                            errorMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Forgetpassword_SetNewPasswordActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}