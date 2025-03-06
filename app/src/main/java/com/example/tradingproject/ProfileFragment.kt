package com.example.tradingproject

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    // Property สำหรับเก็บสถานะว่ารหัสผ่านถูกแสดงหรือซ่อนอยู่
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ค้นหา ImageView สำหรับ toggle รหัสผ่าน ด้วย id ใหม่
        val passwordToggleIcon = view.findViewById<ImageView>(R.id.passwordToggleIcon)
        setupPasswordToggle(view)
        passwordToggleIcon.setOnClickListener {
            togglePasswordVisibility(view)
        }
    }

    private fun setupPasswordToggle(rootView: View) {
        // ค้นหา EditText ด้วย id ใหม่
        val password = rootView.findViewById<EditText>(R.id.edit_password)
        val originalTypeface: Typeface = password.typeface ?: Typeface.DEFAULT

        // กำหนดรูปแบบเริ่มต้นเป็นแบบรหัสผ่าน
        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        password.transformationMethod = PasswordTransformationMethod.getInstance()
        password.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            0,
            R.drawable.lock_password, // ไอคอนสำหรับล็อค
            0
        )
        password.typeface = originalTypeface

        // ตั้ง onTouchListener เพื่อรองรับการแตะที่ drawable ด้านขวาของ EditText
        password.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd: Drawable? = password.compoundDrawablesRelative[2]
                if (drawableEnd != null && event.rawX >= (password.right - drawableEnd.bounds.width() - password.paddingEnd)) {
                    togglePasswordVisibility(rootView)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // เมื่อ EditText ได้รับ focus ให้เคลียร์ข้อความ (ถ้าต้องการ)
        password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                password.text.clear()
            }
        }
    }

    private fun togglePasswordVisibility(rootView: View) {
        val password = rootView.findViewById<EditText>(R.id.edit_password)
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            // แสดงรหัสผ่าน
            password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            password.transformationMethod = null
            password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.unlocked_password, // ไอคอนสำหรับปลดล็อค
                0
            )
        } else {
            // ซ่อนรหัสผ่าน
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            password.transformationMethod = PasswordTransformationMethod.getInstance()
            password.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.lock_password,
                0
            )
        }
        // คงฟอนต์เดิมและเลื่อนเคอร์เซอร์ไปตำแหน่งท้ายข้อความ
        password.typeface = password.typeface
        password.post { password.setSelection(password.text.length) }
    }
}
