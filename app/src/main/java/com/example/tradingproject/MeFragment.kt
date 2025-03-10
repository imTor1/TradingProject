package com.example.tradingproject

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException

class MeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logout = view.findViewById<LinearLayout>(R.id.logout_button)
        val profile = view.findViewById<LinearLayout>(R.id.profile)
        val Check_UsStock = view.findViewById<LinearLayout>(R.id.Check_UsStock)
        val Check_ThStock = view.findViewById<LinearLayout>(R.id.Check_ThStock)


        Check_ThStock.setOnClickListener {
            findNavController().navigate(R.id.mystock_th)
        }
        Check_UsStock.setOnClickListener {
            findNavController().navigate(R.id.mystock_us)
        }

        profile.setOnClickListener{
            findNavController().navigate(R.id.nav_profile)
        }

        logout.setOnClickListener {
            logout.setOnClickListener {
                // ✅ ลบข้อมูลผู้ใช้ใน SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    remove("TOKEN")
                    remove("USER_ID")
                    remove("EMAIL")
                    remove("USERNAME")
                    remove("PROFILE_IMAGE")
                    apply()
                }
                // ✅ นำทางไปที่หน้า LoginPage และเคลียร์ Back Stack
                val intent = Intent(requireContext(), LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

        }

        getUserProfile(view)
    }

    private fun getUserProfile(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
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
                            val username = jsonObject.optString("username", "")
                            val profileImageUrl = jsonObject.optString("profileImage", "")

                            view.findViewById<TextView>(R.id.Username).setText(username)
                            val profileImageView = view.findViewById<ImageView>(R.id.profile_img)
                            if (profileImageUrl.isNotEmpty() && profileImageUrl != "No image uploaded") {
                                val fullUrlImg = getString(R.string.root_url) + profileImageUrl
                                Log.d("ImgProfile","$fullUrlImg")
                                Glide.with(this@MeFragment)
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



}