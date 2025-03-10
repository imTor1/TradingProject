package com.example.tradingproject

import StockAdapter
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class InvestFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_invest, container, false)
       // RecommenStock(view)
        val searchbar = view.findViewById<TextView>(R.id.search_bar)
        getUserProfile(view)
        searchbar.setOnClickListener {
            findNavController().navigate(R.id.search)
        }
        RecommenStock(view)
        return view
    }

    private fun RecommenStock(view: View) {
        val recyclerRecommended = view.findViewById<RecyclerView>(R.id.recyclerStock)
        recyclerRecommended.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecommended.setHasFixedSize(true)
        recyclerRecommended.isNestedScrollingEnabled = false

        stockList.clear()

        val apiUrl = getString(R.string.root_url) + getString(R.string.Top10_stock)
        val client = OkHttpClient()
        val request = Request.Builder().url(apiUrl).get().build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext // ✅ ป้องกัน Fragment ถูกถอดออกก่อนโหลดเสร็จ

                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val stocksArray = jsonObject.getJSONArray("topStocks")

                            for (i in 0 until stocksArray.length()) {
                                val stockObj = stocksArray.getJSONObject(i)
                                val stockDetailID = stockObj.optString("StockDetailID", "N/A") // ✅ ใช้ optString ป้องกัน JSONException
                                val stockSymbol = stockObj.optString("StockSymbol", "N/A")
                                val closePrice = stockObj.optString("ClosePrice", "N/A")
                                var changePercentage = stockObj.optString("ChangePercentage", "N/A").trim()

                                // ✅ ตรวจสอบว่าค่า Change เป็นตัวเลขหรือไม่
                                val changeValue = changePercentage.toDoubleOrNull()
                                changePercentage = when {
                                    changeValue == null -> "N/A"
                                    changeValue < 0 -> "$changeValue%"  // แสดงค่าลบตามปกติ
                                    else -> "+$changeValue%" // ✅ ใส่ "+" ข้างหน้าเมื่อเป็นค่าบวก
                                }

                                val flagIcon = when (stockSymbol) {
                                    "INTUCH", "ADVANC", "TRUE", "DITTO", "DIF", "INSET", "JMART", "INET", "JAS", "HUMAN" -> R.drawable.icon_flagth
                                    else -> R.drawable.icon_flagus
                                }

                                stockList.add(StockModel(stockDetailID, stockSymbol, closePrice, changePercentage, flagIcon))
                            }

                            // ✅ สร้าง Adapter และส่งข้อมูล StockDetailID ไปยัง DetailFragment
                            stockAdapter = StockAdapter(stockList) { selectedStock ->
                                val bundle = Bundle().apply {
                                    putString("StockDetailID", selectedStock.StockDetailID)
                                    putString("StockName", selectedStock.StockSymbol)

                                }
                                findNavController().navigate(R.id.nav_detail, bundle) // ✅ ส่ง StockDetailID ไป DetailFragment
                            }
                            recyclerRecommended.adapter = stockAdapter
                            stockAdapter.notifyDataSetChanged()

                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error parsing stock data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch stock data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getUserProfile(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(requireContext(), "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
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
                            val username = jsonObject.optString("username", "")
                            val profileImageUrl = jsonObject.optString("profileImage", "")

                            view.findViewById<TextView>(R.id.Username).setText(username)
                            val profileImageView = view.findViewById<ImageView>(R.id.profile_img)
                            profileImageView.setOnClickListener {
                                findNavController().navigate(R.id.nav_me)
                            }

                            if (profileImageUrl.isNotEmpty() && profileImageUrl != "No image uploaded") {
                                val fullUrlImg = getString(R.string.root_url) + profileImageUrl
                                Log.d("ImgProfile","$fullUrlImg")
                                Glide.with(this@InvestFragment)
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