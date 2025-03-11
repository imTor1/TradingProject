package com.example.tradingproject

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import android.widget.TextView
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment() {
    private var imageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it // อัปเดตค่า imageUri
            loadProfileImage(it) // โหลดรูปใหม่ใน UI
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backbutton = view.findViewById<ImageView>(R.id.backbutton)
        setupPasswordToggle(view)
        backbutton.setOnClickListener{
            findNavController().popBackStack()
        }
        val editName = view.findViewById<EditText>(R.id.edit_name)
        val editBirthday = view.findViewById<EditText>(R.id.edit_birthday)
        val updateProfileButton = view.findViewById<Button>(R.id.UpdateNewProfile_button)
        val edit_password = view.findViewById<EditText>(R.id.edit_password)
        val spinnerGender = view.findViewById<Spinner>(R.id.spinner_gender)
        val genderOptions = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, genderOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerGender.adapter = adapter

        editBirthday.setOnClickListener {
            showDatePicker(editBirthday)
        }

        // ✅ ให้ผู้ใช้เลือกภาพโปรไฟล์ใหม่
        val profileImageView = view.findViewById<ImageView>(R.id.profile_img)
        profileImageView.setOnClickListener {
            pickImage.launch("image/*")
        }
        updateProfileButton.setOnClickListener {
            val newUserName = editName.text.toString().trim()
            val newGender = spinnerGender.selectedItem.toString()
            val newBirthday = editBirthday.text.toString().trim()
            val NewPassword = edit_password.text.toString().trim()

            updateUserProfile(newUserName, newGender, newBirthday, imageUri)

            findNavController().popBackStack()
        }

        getUserProfile(view)
    }

    private fun setupPasswordToggle(rootView: View) {
        val password = rootView.findViewById<EditText>(R.id.edit_password)
        val originalTypeface: Typeface = password.typeface ?: Typeface.DEFAULT

        // กำหนดรูปแบบเริ่มต้นเป็นแบบรหัสผ่าน
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


    private fun getUserProfile(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(requireContext(), "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show()
            return
        }
        val client = OkHttpClient()
        val url = requireContext().getString(R.string.root_url) + getString(R.string.ShowProfile) + "/$userId/profile"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val email = jsonObject.optString("email", "")
                            val username = jsonObject.optString("username", "")
                            val gender = jsonObject.optString("gender", "")
                            val birthday = jsonObject.optString("birthday", "")
                            val age = jsonObject.optInt("age", 0)
                            val profileImageUrl = jsonObject.optString("profileImage", "")

                            val formattedBirthday = formatDate(birthday)
                            Log.d("UserProfile", "birthday: $birthday, formatted: $formattedBirthday, age: $age")


                            //แสดงข้อมูลใน UI
                            view.findViewById<EditText>(R.id.edit_email).setText("$email")
                            view.findViewById<EditText>(R.id.edit_name).setText("$username")
                            view.findViewById<EditText>(R.id.edit_birthday).setText("$formattedBirthday")
                            view.findViewById<EditText>(R.id.edit_Age).setText("$age")
                            view.findViewById<EditText>(R.id.edit_Age).setText("$age")

                            val spinnerGender = view.findViewById<Spinner>(R.id.spinner_gender)
                            val genderOptions = arrayOf("Male", "Female", "Other")
                            val index = genderOptions.indexOf(gender)
                            if (index != -1) {
                                spinnerGender.setSelection(index)
                            }

                            //โหลดภาพโปรไฟล์ด้วย Glide
                            val profileImageView = view.findViewById<ImageView>(R.id.profile_img)
                            if (profileImageUrl.isNotEmpty() && profileImageUrl != "No image uploaded") {
                                val fullUrlImg = getString(R.string.root_url) + profileImageUrl
                                Log.d("ImgProfile","$fullUrlImg")
                                Glide.with(this@ProfileFragment)
                                    .load(fullUrlImg)
                                    .centerCrop()
                                    .into(profileImageView)
                            } else {
                                profileImageView.setImageResource(R.drawable.profile)
                            }

                        } catch (jsonEx: Exception) {
                            Toast.makeText(requireContext(), "Error parsing profile data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("error", "Failed to load profile")
                        } catch (jsonEx: Exception) {
                            responseBody ?: "Unknown error"
                        }

                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDate(birthday: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // ตั้งค่าให้ใช้เวลาตาม UTC
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // รูปแบบที่ต้องการ
            val date = inputFormat.parse(birthday)
            date?.let { outputFormat.format(it) } ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }



    private fun updateUserProfile(username: String, gender: String, birthday: String, imgUri: Uri?) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""

        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(requireContext(), "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val url = requireContext().getString(R.string.root_url) + "/api/users/$userId/profile"

        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("gender", gender)
            .addFormDataPart("birthday", birthday) // ส่งเป็น YYYY-MM-DD
            //.addFormDataPart("")

        imgUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_img", ".jpg", requireContext().cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val filename = tempFile.name
            requestBodyBuilder.addFormDataPart("profileImage", filename, RequestBody.create(mediaType, tempFile))
        }

        val requestBody = requestBodyBuilder.build()

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Authorization", "Bearer $token") // ส่ง Token
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val message = jsonObject.optString("message", "Profile updated successfully")
                            val updatedUser = jsonObject.optJSONObject("userProfile")

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                            updatedUser?.let {
                                // 🔹 อัปเดตข้อมูลใน SharedPreferences
                                with(sharedPreferences.edit()) {
                                    putString("USERNAME", it.optString("username", ""))
                                    putString("GENDER", it.optString("gender", ""))
                                    putString("BIRTHDAY", it.optString("birthday", ""))
                                    putString("PROFILE_IMAGE", it.optString("profileImage", ""))
                                    apply()
                                }

                                // ✅ โหลดรูปโปรไฟล์ใหม่
                                loadUpdatedProfileImage(it.optString("profileImage", ""))
                            }

                        } catch (jsonEx: Exception) {
                            Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("error", "Error updating profile")
                        } catch (jsonEx: Exception) {
                            responseBody ?: "Unknown error"
                        }

                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUpdatedProfileImage(profileImageUrl: String) {
        val profileImageView = view?.findViewById<ImageView>(R.id.profile_img)

        if (profileImageUrl.isNotEmpty() && profileImageUrl != "No image uploaded") {
            val fullUrlImg = requireContext().getString(R.string.root_url) + profileImageUrl
            Glide.with(requireContext())
                .load(fullUrlImg)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(profileImageView!!)
        } else {
            profileImageView?.setImageResource(R.drawable.profile)
        }
    }

    // ✅ โหลดรูปที่เลือกไปแสดงผล
    private fun loadProfileImage(uri: Uri) {
        val profileImageView = view?.findViewById<ImageView>(R.id.profile_img)
        Glide.with(requireContext())
            .load(uri)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(profileImageView!!)
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val eighteenYearsAgo = calendar.apply { add(Calendar.YEAR, -18) }.timeInMillis

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = eighteenYearsAgo

        datePickerDialog.show()
    }
}
