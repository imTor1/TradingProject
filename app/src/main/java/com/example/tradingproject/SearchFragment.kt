package com.example.tradingproject

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class SearchFragment : Fragment() {
    private lateinit var stockSearchAdapter: StockSearchAdapter
    private lateinit var stockSearch : StockSearch
    private var searchJob: Job? = null
    private lateinit var bundle: Bundle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backbutton = view.findViewById<ImageView>(R.id.backbutton)
        val editSearch = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.SearchStock)

        stockSearchAdapter = StockSearchAdapter(emptyList()) { stock ->
            val bundle = Bundle().apply {
                putString("StockDetailID", stock.StockDetailID)
                putString("StockName", stock.StockSymbol)

            }
            findNavController().navigate(R.id.nav_detail,bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = stockSearchAdapter

        backbutton.setOnClickListener {
            findNavController().popBackStack()
        }

        // 🔹 ทำให้ค้นหาเป็นแบบ Real-Time
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel() // ยกเลิก Task เก่าถ้ามี

                val query = s.toString().trim()

                if (query.isNotEmpty()) {
                    searchJob = lifecycleScope.launch {
                        delay(300) // ลดการเรียก API ถี่เกินไป
                        Search_Stock(query)
                    }
                } else {
                    stockSearchAdapter.updateData(emptyList()) // ล้างผลลัพธ์ถ้าไม่มีข้อความ
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun Search_Stock(query: String) {
        val apiUrl = getString(R.string.root_url) + getString(R.string.Search) + "?query=$query"
        val client = OkHttpClient()
        val request = Request.Builder().url(apiUrl).get().build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext

                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val stocksArray = jsonObject.getJSONArray("results")

                            val stockList = mutableListOf<StockSearch>()

                            for (i in 0 until stocksArray.length()) {
                                val stockObj = stocksArray.getJSONObject(i)

                                val stockSymbol = stockObj.getString("StockSymbol")
                                val companyName = stockObj.getString("CompanyName") // 🔹 แก้ไขให้ใช้ชื่อบริษัทแทน Market

                                val flagIcon = when (stockSymbol) {
                                    "INTUCH", "ADVANC", "TRUE", "DITTO", "DIF", "INSET", "JMART", "INET", "JAS", "HUMAN" -> R.drawable.icon_flagth
                                    else -> R.drawable.icon_flagus
                                }
                                val stockDetailID = stockObj.optString("StockDetailID", "N/A") // ✅ ป้องกัน Error

                                val stock = StockSearch(stockDetailID,stockSymbol, companyName,flagIcon)
                                stockList.add(stock)
                            }


                            stockSearchAdapter.updateData(stockList)

                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error parsing stock data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        stockSearchAdapter.updateData(emptyList()) // 🔹 ล้างผลลัพธ์เมื่อไม่มีข้อมูล
                        Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
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
}
