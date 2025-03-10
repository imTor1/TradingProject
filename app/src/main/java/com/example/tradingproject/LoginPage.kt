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
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.io.IOException


class LoginPage : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        enableEdgeToEdge()
        val btnRegister = findViewById<TextView>(R.id.btnregister)
        val loginButton = findViewById<Button>(R.id.login_button)
        val googleLoginButton = findViewById<Button>(R.id.logingoogle)
        val forgetPassword = findViewById<TextView>(R.id.forgetpassword)

        btnRegister.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        forgetPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG


        forgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgetpasswordEmailActivity::class.java))
        }
        PasswordLockNShow()

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

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
                if (drawableEnd != null &&
                    event.rawX >= (password.right - drawableEnd.bounds.width() - password.paddingEnd)
                ) {
                    isPasswordVisible = !isPasswordVisible

                    if (isPasswordVisible) {
                        password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        password.transformationMethod = null
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.unlocked_password,
                            0
                        )
                    } else {
                        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        password.transformationMethod = PasswordTransformationMethod.getInstance()
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.lock_password,
                            0
                        )
                    }
                    password.typeface = originalTypeface
                    password.setSelection(password.text.length)
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
    private fun loginUser(email: String, password: String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + "/api/login"
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
                            val message = jsonObject.optString("message", "Welcome To TradeMine")
                            val token = jsonObject.optString("token", "")
                            val userObject = jsonObject.optJSONObject("user")
                            Toast.makeText(this@LoginPage, message, Toast.LENGTH_SHORT).show()
                            if (token.isNotEmpty()) {
                                // ✅ บันทึก Token และข้อมูลผู้ใช้ลง SharedPreferences
                                val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    putString("TOKEN", token)
                                    userObject?.let {
                                        putString("USER_ID", it.optString("id", ""))
                                        putString("EMAIL", it.optString("email", ""))
                                        putString("USERNAME", it.optString("username", ""))
                                        putString("PROFILE_IMAGE", it.optString("profileImage", ""))
                                    }
                                    apply()
                                }
                                // ✅ ไปยังหน้าหลัก MainActivity
                                val intent = Intent(this@LoginPage, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } catch (jsonEx: Exception) {
                            Toast.makeText(this@LoginPage, "Error parsing response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("message", "Login failed : Try again")
                        } catch (jsonEx: Exception) {
                            responseBody
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginPage, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}



