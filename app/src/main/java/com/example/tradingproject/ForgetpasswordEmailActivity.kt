package com.example.tradingproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                sendForgotPasswordRequest(emailEditText)
            } else {
                Toast.makeText(this, "กรุณากรอกอีเมล", Toast.LENGTH_SHORT).show()
            }
        }

        btnbackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendForgotPasswordRequest(email: String) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url(getString(R.string.root_url) + getString(R.string.forgetpassword_Email)) // ตรวจสอบ URL ให้ตรงกับ API
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ForgetpasswordEmailActivity,
                        "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ได้",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    response.use {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                "ส่ง OTP ไปที่อีเมลเรียบร้อย",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@ForgetpasswordEmailActivity,
                                "เกิดข้อผิดพลาด: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

}
