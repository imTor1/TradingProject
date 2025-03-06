package com.example.tradingproject

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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


    private fun BtnController(){
        val btnbackToLogin = findViewById<TextView>(R.id.btnbackToLogin)
        val btnRegisterEmail = findViewById<Button>(R.id.btnRegisterEmail)
        btnbackToLogin.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        btnRegisterEmail.setOnClickListener(){
            val intent = Intent(this, RegisterEmailOTPActivity::class.java)
            startActivity(intent)
        }

        btnbackToLogin.setOnClickListener(){
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

    }
}