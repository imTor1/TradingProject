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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        val btnRegister = findViewById<TextView>(R.id.btnregister)
        val loginButton = findViewById<Button>(R.id.login_button)
        val googleLoginButton = findViewById<Button>(R.id.logingoogle)
        val forgetPassword = findViewById<TextView>(R.id.forgetpassword)

        forgetPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        btnRegister.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        PasswordLockNShow()

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterEmailActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.edit_email)
            val passwordEditText = findViewById<EditText>(R.id.edit_password)

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // เรียกใช้งานฟังก์ชัน loginUser เพื่อทำการเข้าสู่ระบบ
                loginUser(email, password)
            } else {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * ฟังก์ชัน loginUser ใช้สำหรับส่ง HTTP POST Request เพื่อเข้าสู่ระบบผ่าน API
     */
    private fun loginUser(email: String, password: String) {
        // สร้าง OkHttpClient instance
        val client = OkHttpClient()

        // URL ของ API ที่ใช้เข้าสู่ระบบ (เปลี่ยนเป็น URL ที่ใช้งานจริง)
        val url = "https://yourapi.com/api/login"

        // สร้าง RequestBody โดยส่งข้อมูล email และ password
        val formBody: RequestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        // สร้าง Request สำหรับ POST
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        // เรียกใช้งาน network request ใน Coroutine บน Dispatchers.IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // หากเข้าสู่ระบบสำเร็จ สามารถทำการ parse response หากต้องการ
                        Toast.makeText(this@LoginPage, "Login Successful", Toast.LENGTH_SHORT).show()
                        // เปลี่ยนหน้าไปยัง MainActivity
                        val intent = Intent(this@LoginPage, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // แสดงรหัส error ที่ได้จาก server
                        Toast.makeText(this@LoginPage, "Login failed: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginPage, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun PasswordLockNShow() {
        val password = findViewById<EditText>(R.id.edit_password)
        var isPasswordVisible = false
        val originalTypeface: Typeface = password.typeface ?: Typeface.DEFAULT

        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.transformationMethod = PasswordTransformationMethod.getInstance()
        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.lock_password,
            0
        )
        password.typeface = originalTypeface

        password.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd: Drawable? = password.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (password.right - drawableEnd.bounds.width() - password.paddingEnd)) {
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

        password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                password.text.clear()
            }
        }
    }
}
