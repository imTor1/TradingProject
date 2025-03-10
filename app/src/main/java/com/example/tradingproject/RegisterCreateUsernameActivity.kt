package com.example.tradingproject

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterCreateUsernameActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it // อัปเดต imageUri เมื่อเลือกรูปใหม่
            loadProfileImage(it)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_create_username)

        val editBirthdate = findViewById<EditText>(R.id.edit_birthdate)
        val editName = findViewById<EditText>(R.id.edit_name)
        val Btn_continuesetprofile = findViewById<Button>(R.id.continue_setprofile)
        val spinnerGender = findViewById<Spinner>(R.id.spinner_gender)
        val genderOptions = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, genderOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        spinnerGender.adapter = adapter


        changeProfileImage()

        editBirthdate.setOnClickListener {
            showDatePicker(editBirthdate)
        }

        Btn_continuesetprofile.setOnClickListener {
            val Name = editName.text.toString().trim()
            val dateBirthday = editBirthdate.text.toString().trim()
            val ImgProfile = imageUri
            val newGender = spinnerGender.selectedItem.toString().trim()
            if (Name.isEmpty() || dateBirthday.isEmpty() || newGender.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
            } else {
                SetProfile(Name, ImgProfile, dateBirthday,newGender)
            }
        }


        val btnbackToLogin: TextView = findViewById(R.id.btnbackToLogin)
        btnbackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))

        }

    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val eighteenYearsAgo = calendar.apply { add(Calendar.YEAR, -18) }.timeInMillis

        val datePickerDialog = DatePickerDialog(
            this@RegisterCreateUsernameActivity,
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

        // ✅ กำหนด `maxDate` ไม่ให้เลือกวันเกิดที่อายุน้อยกว่า 18 ปี
        datePickerDialog.datePicker.maxDate = eighteenYearsAgo

        datePickerDialog.show()
    }


    fun changeProfileImage() {
        val profile = findViewById<ImageView>(R.id.profile_image)
        profile.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun loadProfileImage(uri: Uri) {
        val profile = findViewById<ImageView>(R.id.profile_image)
        try {
            Glide.with(this)
                .load(uri)
                .into(profile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun SetProfile(editUserName: String, ImgProfile: Uri?, Birthday: String,newGender : String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + getString(R.string.set_profile)

        // ดึง Token จาก SharedPreferences
        val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""

        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("newUsername", editUserName)
            .addFormDataPart("birthday", Birthday)
            .addFormDataPart("gender", newGender)


        ImgProfile?.let { uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_img", ".jpg", cacheDir)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val filename = tempFile.name
            requestBodyBuilder.addFormDataPart("picture", filename, RequestBody.create(mediaType, tempFile))
        }

        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
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
                            val message = jsonObject.optString("message", "Profile updated successfully")
                            val newToken = jsonObject.optString("token", "")
                            val userObject = jsonObject.optJSONObject("user")

                            Toast.makeText(this@RegisterCreateUsernameActivity, message, Toast.LENGTH_SHORT).show()

                            if (newToken.isNotEmpty()) {
                                with(sharedPreferences.edit()) {
                                    putString("TOKEN", newToken)
                                    apply()
                                }
                            }

                            userObject?.let {
                                with(sharedPreferences.edit()) {
                                    putString("USER_ID", it.optString("id", ""))
                                    putString("EMAIL", it.optString("email", ""))
                                    putString("USERNAME", it.optString("username", ""))
                                    putString("PROFILE_IMAGE", it.optString("profileImage", ""))
                                    apply()
                                }
                            }

                            val intent = Intent(this@RegisterCreateUsernameActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } catch (jsonEx: Exception) {
                            Toast.makeText(this@RegisterCreateUsernameActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = try {
                            val jsonObject = JSONObject(responseBody)
                            jsonObject.optString("message", "Error updating profile")
                        } catch (jsonEx: Exception) {
                            responseBody ?: "Unknown error"
                        }

                        Toast.makeText(this@RegisterCreateUsernameActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterCreateUsernameActivity, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
