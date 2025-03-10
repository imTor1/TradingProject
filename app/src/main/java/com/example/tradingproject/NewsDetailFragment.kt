package com.example.tradingproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class NewsDetailFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var sentimentTextView: TextView
    private lateinit var sourceTextView: TextView
    private lateinit var confidenceTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var openUrlTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news_detail, container, false)

        // เชื่อม View กับ Layout
        titleTextView = view.findViewById(R.id.newsTitle)
        dateTextView = view.findViewById(R.id.newsDate)
        sentimentTextView = view.findViewById(R.id.newsSentiment)
        sourceTextView = view.findViewById(R.id.newsSource)
        //confidenceTextView = view.findViewById(R.id.newsConfidence)
        //contentTextView = view.findViewById(R.id.newsContent)
        openUrlTextView = view.findViewById(R.id.newsUrl)

        // รับค่า NewsID จาก Arguments
        val newsId = arguments?.getString("NEWS_ID")
        if (newsId != null) {
            fetchNewsDetail(newsId)
        } else {
            Toast.makeText(requireContext(), "ไม่พบข่าว", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchNewsDetail(newsId: String) {
        val client = OkHttpClient()
        val url = getString(R.string.root_url) + "/api/news-detail?id=$newsId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "โหลดรายละเอียดข่าวล้มเหลว", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "ไม่สามารถโหลดรายละเอียดข่าวได้", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                response.body?.let { responseBody ->
                    val jsonString = responseBody.string()
                    val jsonObject = JSONObject(jsonString)

                    requireActivity().runOnUiThread {
                        titleTextView.text = jsonObject.getString("Title")
                        dateTextView.text = "วันที่: ${jsonObject.getString("PublishedDate")}"
                        sentimentTextView.text = "Sentiment: ${jsonObject.getString("Sentiment")}"
                        sourceTextView.text = "แหล่งที่มา: ${jsonObject.getString("Source")}"

                        // ใช้ค่าความแม่นยำจาก API ที่แปลงเป็นเปอร์เซ็นต์แล้ว
                        confidenceTextView.text = "ความแม่นยำ: ${jsonObject.getString("ConfidenceScore")}"

                        contentTextView.text = jsonObject.getString("Content")

                        val url = jsonObject.getString("URL")
                        openUrlTextView.text = "อ่านต่อ"
                        openUrlTextView.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    }
                }
            }
        })
    }





}
