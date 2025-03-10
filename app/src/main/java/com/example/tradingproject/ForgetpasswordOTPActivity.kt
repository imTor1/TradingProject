package com.example.tradingproject

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ForgetpasswordOTPActivity : AppCompatActivity() {
    private lateinit var otpEditTexts: List<EditText>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgetpassword_otpactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val SentOtp : TextView = findViewById(R.id.SentOtp)
        val editOtp1: EditText = findViewById(R.id.editOtp1)
        val editOtp2: EditText = findViewById(R.id.editOtp2)
        val editOtp3: EditText = findViewById(R.id.editOtp3)
        val editOtp4: EditText = findViewById(R.id.editOtp4)
        val editOtp5: EditText = findViewById(R.id.editOtp5)
        val editOtp6: EditText = findViewById(R.id.editOtp6)
        val btnRegisterOTP = findViewById<Button>(R.id.btnRegisterOTP)
        val email = intent.getStringExtra("email") ?: ""

        otpEditTexts = listOf(editOtp1, editOtp2, editOtp3, editOtp4, editOtp5, editOtp6)
        setupOtpEditTexts()
        btnRegisterOTP.setOnClickListener {

            val otp = otpEditTexts.joinToString(separator = "") { it.text.toString() }
            Log.d("VerifyOtp", "Verifying OTP for email: $email, OTP: $otp")

            SentOtp.setOnClickListener {
                verifyOtp(email, otp)
            }

            val btnbackToLogin: TextView = findViewById(R.id.btnbackToLogin)
            btnbackToLogin.setOnClickListener {
                startActivity(Intent(this, LoginPage::class.java))

            }
            if (otp.length == 6) {
                verifyOtp(email, otp)
            } else {
                Toast.makeText(this, "กรุณากรอก OTP ให้ครบ 6 หลัก", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpEditTexts() {
        otpEditTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpEditTexts.size - 1) {
                        otpEditTexts[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isEmpty() && index > 0) {
                        otpEditTexts[index - 1].requestFocus()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun verifyOtp(email: String, otp: String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + getString(R.string.verify_otp_email_register)
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("otp", otp)
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
                        val message = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("message", "OTP ถูกต้อง")
                        } catch (jsonEx: Exception) {
                            responseBody
                        }
                        Toast.makeText(this@ForgetpasswordOTPActivity, message, Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(
                            this@ForgetpasswordOTPActivity,
                            Forgetpassword_SetNewPasswordActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        // ถ้า response ไม่สำเร็จ ให้นำ error message ที่ส่งกลับมาแสดง
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("error", "เกิดข้อผิดพลาด")
                        } catch (jsonEx: Exception) {
                            responseBody
                        }
                        Toast.makeText(this@ForgetpasswordOTPActivity, errorMsg, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ForgetpasswordOTPActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}