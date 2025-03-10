package com.example.tradingproject

import StockAdapter
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class HomeFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private lateinit var favoriteStockAdapter: FavoriteStockAdapter
    private val favoriteStock = mutableListOf<FavoriteStock>()
    private val stockList = mutableListOf<StockModel>()
    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        // FavoriteStock(view)
        RecommenStock(view)
        autoScrollRecyclerView(view)
        val searchbar = view.findViewById<TextView>(R.id.search_bar)
        val addfavorite = view.findViewById<ImageView>(R.id.addfavorite)
        //val newspage = view.findViewById<ImageView>(R.id.enter_newspage)
        val webView = view.findViewById<WebView>(R.id.tradingViewWebHome)


        addfavorite.setOnClickListener {
            findNavController().navigate(R.id.search)
        }
        searchbar.setOnClickListener {
            findNavController().navigate(R.id.search)
        }


        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // ป้องกัน WebView เปิดลิงก์ใน Browser
        webView.webViewClient = WebViewClient()

        // โหลด TradingView HTML
        val htmlData = """
            <html>
            <body style="margin:0;padding:0;">
                <div class="tradingview-widget-container">
                    <div class="tradingview-widget-container__widget"></div>
                    <script type="text/javascript" src="https://s3.tradingview.com/external-embedding/embed-widget-symbol-overview.js">
                    {
                      "symbols": [
                        [
                                    ["Apple","AAPL|1D"],
                                    ["Google","GOOGL|1D"],
                                    ["Microsoft","MSFT|1D"],
                                    ["NASDAQ","AMZN|1D"],
                                    ["NASDAQ","TSLA|1D"],
                                    ["NASDAQ","NVDA|1D"],
                                    ["BCBA","TSMC|1D"],
                                    ["NASDAQ","AMD|1D"],
                                    ["NASDAQ","AVGO|1D"],
                                    ["NASDAQ","META|1D"]
                        ]
                      ],
                      "chartOnly": false,
                      "width": "100%",
                      "height": "400",
                      "locale": "en",
                      "colorTheme": "dark",
                      "autosize": true,
                      "showVolume": false,
                      "showMA": false,
                      "hideDateRanges": false,
                      "hideMarketStatus": false,
                      "hideSymbolLogo": false,
                      "scalePosition": "right",
                      "scaleMode": "Normal",
                      "fontFamily": "-apple-system, BlinkMacSystemFont, Trebuchet MS, Roboto, Ubuntu, sans-serif",
                      "fontSize": "12",
                      "noTimeScale": false,
                      "valuesTracking": "1",
                      "changeMode": "price-and-percent",
                      "chartType": "area",
                      "maLineColor": "#2962FF",
                      "maLineWidth": 1,
                      "maLength": 9,
                      "headerFontSize": "medium",
                      "lineWidth": 2,
                      "lineType": 0,
                      "dateRanges": [
                        "1d|1",
                        "1m|30",
                        "3m|60",
                        "12m|1D",
                        "60m|1W",
                        "all|1M"
                      ]
                    }
                    </script>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)



        getUserProfile(view)
        FavoriteStockShow(view)
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

    private fun autoScrollRecyclerView(view: View) {
        val recyclerStock = view.findViewById<RecyclerView>(R.id.recyclerStock)
        val handler = Handler(Looper.getMainLooper())

        val scrollDistance = 5   // ระยะเลื่อน (ยิ่งน้อยยิ่งช้า)
        val scrollInterval = 50L // ค่ามิลลิวินาที (ยิ่งสูงยิ่งช้า)
        val restartDelay = 1000L // หยุด 1 วินาทีก่อนกลับมา Scroll

        var isUserScrolling = false

        val runnable = object : Runnable {
            override fun run() {
                if (!isUserScrolling) {
                    val layoutManager = recyclerStock.layoutManager as LinearLayoutManager

                    if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1) {
                        recyclerStock.scrollToPosition(0)
                    } else {
                        recyclerStock.smoothScrollBy(scrollDistance, 0)
                    }

                    handler.postDelayed(this, scrollInterval)
                }
            }
        }

        handler.post(runnable)

        recyclerStock.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        isUserScrolling = true
                        handler.removeCallbacks(runnable)
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        handler.postDelayed({
                            isUserScrolling = false
                            handler.post(runnable)
                        }, restartDelay)
                    }
                }
            }
        })
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
                                Glide.with(this@HomeFragment)
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



    private fun FavoriteStockShow(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        val recyclerRecommended = view.findViewById<RecyclerView>(R.id.recyclerFavoriteStock)
        recyclerRecommended.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecommended.setHasFixedSize(true)
        recyclerRecommended.isNestedScrollingEnabled = false

        val apiUrl = getString(R.string.root_url) + getString(R.string.FavoriteStock)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + token) // ✅ ใส่ Token เพื่อยืนยันตัวตน
            .get()
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext // ✅ ป้องกัน Fragment ถูกปิดก่อนโหลดเสร็จ

                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        try {
                            Log.d("FavoriteStockShow", "Response: $responseBody") // ✅ Debug JSON Response

                            val stocksArray = JSONArray(responseBody)
                            val favoriteStockList = mutableListOf<FavoriteStock>()

                            for (i in 0 until stocksArray.length()) {
                                val stockObj = stocksArray.getJSONObject(i)
                                val stockSymbol = stockObj.optString("StockSymbol", "")
                                val lastPrice = stockObj.optString("LastPrice", "")
                                val lastChange = stockObj.optString("LastChange", "")

                                favoriteStockList.add(FavoriteStock(stockSymbol, lastPrice, lastChange))
                            }

                            // ✅ สร้าง Adapter พร้อมกับฟังก์ชันการคลิกเพื่อไปหน้า Detail
                            val favoriteAdapter = FavoriteStockAdapter(favoriteStockList) { stock ->
                                val bundle = Bundle().apply {
                                    putString("StockName", stock.StockSymbol)
                                }
                                findNavController().navigate(R.id.nav_detail, bundle) // ✅ ส่งค่าไปหน้า Detail
                            }

                            // ✅ เพิ่ม ItemTouchHelper สำหรับการเลื่อนเพื่อลบ
                            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT) {
                                override fun onMove(
                                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                                ): Boolean {
                                    return false
                                }

                                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                    val position = viewHolder.adapterPosition
                                    val stockToRemove = favoriteStockList[position]

                                    // 🔥 เรียกใช้ฟังก์ชันลบหุ้นจากรายการโปรด
                                    removeFromFavorites(stockToRemove.StockSymbol)

                                    // ลบจาก RecyclerView
                                    favoriteStockList.removeAt(position)
                                    favoriteAdapter.notifyItemRemoved(position)
                                }
                            })
                            itemTouchHelper.attachToRecyclerView(recyclerRecommended)

                            recyclerRecommended.adapter = favoriteAdapter
                            favoriteAdapter.notifyDataSetChanged()

                        } catch (e: Exception) {
                            Log.e("FavoriteStockShow", "JSON Parsing Error: ${e.message}") // ✅ Debug JSON Error
                            Toast.makeText(requireContext(), "Error parsing stock data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("FavoriteStockShow", "Failed to fetch: ${response.code}") // ✅ Debug API Error
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("FavoriteStockShow", "Network Error: ${e.message}") // ✅ Debug Network Error
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun removeFromFavorites(stockSymbol: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""

        val apiUrl = getString(R.string.root_url) + "/api/favorites"
        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("stock_symbol", stockSymbol)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(apiUrl)
            .delete(requestBody)  // ✅ ใช้ DELETE แทน POST
            .addHeader("Authorization", "Bearer $token")
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // เมื่อสำเร็จ
                        Toast.makeText(requireContext(), "Stock removed from favorites", Toast.LENGTH_SHORT).show()

                        val position = favoriteStock.indexOfFirst { it.StockSymbol == stockSymbol }
                        if (position != -1) {
                            favoriteStock.removeAt(position)
                            favoriteStockAdapter.notifyItemRemoved(position)
                        }

                    } else {
                        val errorMessage = JSONObject(responseBody ?: "{}").optString("error", "Failed to remove stock")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



}

