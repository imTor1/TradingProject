package com.example.tradingproject

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.drawable.Drawable
import android.text.method.PasswordTransformationMethod
import android.widget.Button


class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val email = findViewById<EditText>(R.id.edit_email)
        val password = findViewById<EditText>(R.id.edit_password)
        var loginButton = findViewById<Button>(R.id.login_button)
        var isPasswordVisible = false;
        val textView = findViewById<TextView>(R.id.policy1)
        val register = findViewById<TextView>(R.id.register)
        val forgetpassword = findViewById<TextView>(R.id.forgetpassword)

        //Add color Hilight Policy
        val highlightedText = "By proceeding, you agree to the terms of use of the <font color='#F0A500'><b>Stock app</b></font> and confirm"
        textView.text = Html.fromHtml(highlightedText, Html.FROM_HTML_MODE_LEGACY)

        //underline Text
        register.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        forgetpassword.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        //Set Lock & Unlock password
        val originalTypeface: Typeface = password.typeface ?: Typeface.DEFAULT
        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.transformationMethod = PasswordTransformationMethod.getInstance()
        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.lock_password, 0) // ไอคอนเริ่มต้น
        password.typeface = originalTypeface // ✅ ใช้ฟอนต์เดิม ป้องกันฟอนต์ห่าง

        password.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd: Drawable? = password.compoundDrawablesRelative[2] // ตรวจสอบ drawableEnd
                if (drawableEnd != null && event.rawX >= (password.right - drawableEnd.bounds.width() - password.paddingEnd)) {
                    isPasswordVisible = !isPasswordVisible // สลับสถานะ Show/Hide Password

                    if (isPasswordVisible) {
                        password.setRawInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) // ✅ ใช้ `setRawInputType()` แทน `setInputType()`
                        password.transformationMethod = null // ✅ ลบการซ่อนรหัสผ่าน
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.unlocked_password, 0) // เปลี่ยนเป็นไอคอนเปิดตา
                    } else {
                        password.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) // ✅ ใช้ `setRawInputType()`
                        password.transformationMethod = PasswordTransformationMethod.getInstance() // ✅ กลับไปซ่อนรหัสผ่าน
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.lock_password, 0) // เปลี่ยนเป็นไอคอนปิดตา
                    }

                    // ✅ ป้องกันฟอนต์ห่าง โดยใช้ `setTypeface()` แทน `originalTypeface`
                    password.setTypeface(password.typeface, Typeface.NORMAL)

                    // ✅ บังคับอัปเดต UI เพื่อให้เคอร์เซอร์อยู่ที่ท้ายข้อความ
                    password.post {
                        password.setSelection(password.text.length)
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        //Set delete Text Password When Click
        password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                password.text.clear() // เคลียร์เมื่อได้รับโฟกัส
            }
        }

        loginButton.setOnClickListener(){
            email.text.clear()
            password.text.clear()
        }



    }
}