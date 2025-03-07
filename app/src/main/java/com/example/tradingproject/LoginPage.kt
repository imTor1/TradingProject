package com.example.tradingproject

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.Drawable
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        val btnRegister = findViewById<TextView>(R.id.btnregister)
        val loginButton = findViewById<Button>(R.id.login_button)
        val googleLoginButton = findViewById<Button>(R.id.logingoogle)
        val forgetPassword = findViewById<TextView>(R.id.forgetpassword)

        // ตั้งเส้นใต้ให้กับปุ่ม Register และ Forget Password
        forgetPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        btnRegister.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        PasswordLockNShow()


        googleLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        forgetPassword.setOnClickListener{
            val intent = Intent(this, ForgetpasswordEmailActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterEmailActivity::class.java)
            startActivity(intent)
        }

        // กด login ด้วย email/password
        loginButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.edit_email)
            val passwordEditText = findViewById<EditText>(R.id.edit_password)

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val client = OkHttpClient()
        // ใช้ URL นี้สำหรับ Android Emulator
        val url = "http://10.0.2.2:3000/api/login"

        val formBody: RequestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                // อ่าน response body เพื่อช่วย debug
                val responseBody = response.body?.string() ?: "No response body"
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@LoginPage, "Login Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginPage, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@LoginPage, "Login failed: $responseBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginPage, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /**
     * ฟังก์ชันสำหรับสลับแสดง/ซ่อนรหัสผ่าน
     */
    private fun PasswordLockNShow() {
        val password = findViewById<EditText>(R.id.edit_password)
        var isPasswordVisible = false
        val originalTypeface: Typeface = password.typeface ?: Typeface.DEFAULT

        // ตั้งค่าเริ่มต้นให้เป็น password field
        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.transformationMethod = PasswordTransformationMethod.getInstance()
        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.lock_password,
            0
        )
        password.typeface = originalTypeface

        // เมื่อแตะที่ drawable ด้านขวาให้สลับแสดง/ซ่อนรหัสผ่าน
        password.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd: Drawable? = password.compoundDrawablesRelative[2]
                if (drawableEnd != null &&
                    event.rawX >= (password.right - drawableEnd.bounds.width() - password.paddingEnd)
                ) {
                    isPasswordVisible = !isPasswordVisible

                    if (isPasswordVisible) {
                        password.setRawInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                        password.transformationMethod = null
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.unlocked_password,
                            0
                        )
                    } else {
                        password.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        password.transformationMethod = PasswordTransformationMethod.getInstance()
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.lock_password,
                            0
                        )
                    }
                    password.setTypeface(password.typeface, Typeface.NORMAL)
                    password.post { password.setSelection(password.text.length) }
                    return@setOnTouchListener true
                }
            }
            false
        }

        // เมื่อได้รับ focus ให้ล้างข้อความ
        password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                password.text.clear()
            }
        }
    }
}
