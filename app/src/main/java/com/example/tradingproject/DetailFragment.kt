package com.example.tradingproject

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class DetailFragment : Fragment() {

    private lateinit var StockSymbol: TextView
    private lateinit var ClosePrice: TextView
    private lateinit var StockInfo: TextView
    private lateinit var SectorStock : TextView
    private lateinit var IndastryStock : TextView
    private lateinit var PredictionStock : TextView
    private lateinit var TypeStock : TextView
    private lateinit var DateUpdateStock : TextView
    private lateinit var flagimg : ImageView
    private lateinit var TypeStock2 : TextView
    private lateinit var PredictionTrend : TextView
    private lateinit var predictionDate : TextView
    private lateinit var Open : TextView
    private lateinit var Close : TextView
    private lateinit var AvgVolume30D :TextView
    private val client = OkHttpClient()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.backbutton)
        StockSymbol = view.findViewById(R.id.nameStock)
        ClosePrice = view.findViewById(R.id.priceStock)
        StockInfo = view.findViewById(R.id.infoStock)
        SectorStock = view.findViewById(R.id.sectorStock)
        IndastryStock = view.findViewById(R.id.industryStock)
        PredictionStock = view.findViewById(R.id.predictionStock)
        TypeStock = view.findViewById(R.id.typeStock)
        DateUpdateStock = view.findViewById(R.id.dateUpdateStock)
        flagimg = view.findViewById(R.id.flagimg)
        TypeStock2 = view.findViewById(R.id.typeStock2)
        predictionDate = view.findViewById(R.id.predictionDate)
        PredictionTrend = view.findViewById(R.id.predictionTrend)
        Open = view.findViewById(R.id.OpenStock)
        Close =view.findViewById(R.id.CloseStock)
        AvgVolume30D = view.findViewById(R.id.avgVolume30D)
        val AddtoFavorite = view.findViewById<ImageView>(R.id.AddtoFavorite)
        val webView = view.findViewById<WebView>(R.id.tradingViewWeb)


        val stockDetailID = arguments?.getString("StockDetailID")
        val stockName = arguments?.getString("StockName")
        if (stockName != null) {
            fetchStockDetail(stockName)
        }

        AddtoFavorite.setOnClickListener {
            val stockSymbol = stockName.toString().trim()
            AddtoFavorite(stockSymbol)
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.webViewClient = WebViewClient()


        val htmlData = """
            <html>
            <body style="margin:0;padding:0;">
                <div class="tradingview-widget-container">
                    <div class="tradingview-widget-container__widget"></div>
                    <script type="text/javascript" src="https://s3.tradingview.com/external-embedding/embed-widget-symbol-overview.js">
                    {
                      "symbols": [
                        [
                          "$stockName"
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
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun fetchStockDetail(stockSymbol: String) {
        val apiUrl = "${getString(R.string.root_url)}/api/stock-detail/$stockSymbol"
        Log.d("DetailFragment", "Fetching data from: $apiUrl") // ✅ Debug API URL

        val request = Request.Builder().url(apiUrl).get().build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext // ✅ ป้องกัน Fragment ถูกถอดออกขณะโหลดข้อมูล

                    if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                        Log.d("DetailFragment", "Response: $responseBody") // ✅ Debug Response

                        try {
                            val jsonObject = JSONObject(responseBody)

                            val Date = jsonObject.optString("Date","Unknow")

                            val PriceAt = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                val outputDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // 08 March 2025
                                val date = inputFormat.parse(Date)

                                val formattedDate = outputDateFormat.format(date!!) // แปลงเป็นวันที่

                                "$formattedDate "// รวมวันที่และเวลา
                            } catch (e: Exception) {
                                "Unknown Date"
                            }


                            DateUpdateStock.text = "Price at : $PriceAt"

                            val stockSymbol = jsonObject.optString("StockSymbol", "N/A")
                            StockSymbol.text = stockSymbol


                            val predictionTrend = jsonObject.optString("PredictionTrend","")
                            predictionDate.text = predictionTrend

                            val closePrice = jsonObject.optString("ClosePrice", "N/A")
                            val closePriceTHB = jsonObject.optString("ClosePriceTHB", "N/A")
                            ClosePrice.text = "Close Price: $closePrice US ($closePriceTHB THB)"

                            val changePercentage = jsonObject.optString("Change", "N/A")
                            //ChangePercentage.text = "Change: $changePercentage%"


                            val stockType = jsonObject.optString("Type", "N/A")
                            TypeStock.text = stockType
                            TypeStock2.text = stockType


                            val stockIcon = when (stockType) {
                                "TH Stock" -> R.drawable.icon_flagth  // 🇹🇭 ไอคอนธงไทย
                                "US Stock" -> R.drawable.icon_flagus  // 🇺🇸 ไอคอนธงอเมริกา
                                else -> R.drawable.icon_search       // ❓ ไอคอนเริ่มต้นถ้าไม่มีข้อมูล
                            }
                            flagimg.setImageResource(stockIcon)

                            val predictionCloseDateRaw = jsonObject.optString("PredictionCloseDate", "")
                            val formattedPredictionDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                inputFormat.timeZone = TimeZone.getTimeZone("UTC") // ตั้งค่าให้เป็น UTC
                                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // เปลี่ยนเป็น "08 March 2025"
                                val date = inputFormat.parse(predictionCloseDateRaw)
                                outputFormat.format(date!!) // แปลง Date → String
                            } catch (e: Exception) {
                                "Unknown Date"
                            }
                            predictionDate.text = formattedPredictionDate
                            val pricePredictionChange = jsonObject.optString("PricePredictionChange", "N/A")
                            PredictionStock.text = pricePredictionChange

                            if (pricePredictionChange.startsWith("-")) {
                                // ถ้าค่ามีเครื่องหมายลบ ให้เปลี่ยนเป็นสีแดง
                                PredictionStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.red)) // ใช้สีแดง
                            } else {
                                // ถ้าค่าไม่ติดลบ (บวกหรือตัวเลขที่ไม่มีเครื่องหมาย) ให้เปลี่ยนเป็นสีเขียว
                                PredictionStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.green)) // ใช้สีเขียว
                            }


                            val openPrice = jsonObject.optString("Open","~")
                            val closePrice2 = jsonObject.optString("Close","~")
                            val avgVolume30D = jsonObject.optString("AvgVolume30D","~")
                            Open.text = openPrice
                            Close.text = closePrice2
                            AvgVolume30D.text = avgVolume30D



                            val profile = jsonObject.optJSONObject("Profile")
                            val description =profile?.optString("Description","") ?: ""
                            StockInfo.text = "                  $description"
                            val sector = profile?.optString("Sector", "N/A") ?: "N/A"
                            SectorStock.text = sector
                            val industry = profile?.optString("Industry", "N/A") ?: "N/A"
                            IndastryStock.text = industry



                        } catch (e: Exception) {
                            Log.e("DetailFragment", "JSON Parsing Error: ${e.message}") // ✅ Debug JSON Error
                            Toast.makeText(requireContext(), "Error parsing stock data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("DetailFragment", "Fetch Failed - Response Code: ${response.code}")
                        Toast.makeText(requireContext(), "Failed to fetch stock details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("DetailFragment", "Network Error: ${e.message}") // ✅ Debug Network Error
                    Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun AddtoFavorite(stockSymbol: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", "") ?: ""
        val userId = sharedPreferences.getString("USER_ID", "") ?: ""
        val apiUrl = getString(R.string.root_url) + "/api/favorites"
        val client = OkHttpClient()


        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBody = JSONObject().apply {
            put("stock_symbol", stockSymbol)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token") // ✅ ส่ง Token ไปกับ API
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    Log.d("AddToFavorite", "Response Code: ${response.code}") // ✅ Debug Response Code

                    if (response.isSuccessful) {
                        Log.d("AddToFavorite", "Success: $responseBody") // ✅ Debug Response Body
                        Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("AddToFavorite", "Error Response: $responseBody")
                        val errorMessage = JSONObject(responseBody ?: "{}").optString("error", "Failed to add favorite")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("AddToFavorite", "Network Error: ${e.message}") // ✅ Debug Network Error
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




}
