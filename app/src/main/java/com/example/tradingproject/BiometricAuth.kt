package com.example.tradingproject

import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager
import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuth(private val context: Context, private val callback: (Boolean) -> Unit) {

    fun authenticate() {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            (context as FragmentActivity),
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback(true) // ✅ ปลดล็อกสำเร็จ
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback(false) // ❌ ปลดล็อกไม่สำเร็จ
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ล็อกอินด้วยลายนิ้วมือ")
            .setSubtitle("สแกนลายนิ้วมือเพื่อเข้าสู่ระบบ")
            .setNegativeButtonText("ยกเลิก")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // ✅ บันทึกสถานะล็อกอิน
    fun setLoggedIn(isLoggedIn: Boolean) {
        editor.putBoolean("IS_LOGGED_IN", isLoggedIn)
        editor.apply()
    }

    // ✅ ตรวจสอบว่าผู้ใช้เคยล็อกอินมาก่อนหรือไม่
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("IS_LOGGED_IN", false)
    }
}
