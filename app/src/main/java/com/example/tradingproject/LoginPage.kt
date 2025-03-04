package com.example.tradingproject

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
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
    private lateinit var googleSignInClient: GoogleSignInClient

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

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "1111", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่าน", Toast.LENGTH_SHORT).show()
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
                        password.transformationMethod =
                            PasswordTransformationMethod.getInstance()
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


