package com.example.tradingproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class NewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsList>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        val searchbar = view.findViewById<TextView>(R.id.search_bar)
        searchbar.setOnClickListener {
            findNavController().navigate(R.id.search)
        }
        fetchNewsList(view)
        return view
    }

    private fun fetchNewsList(view: View) {
        val recyclerNews = view.findViewById<RecyclerView>(R.id.recyclerNews)
        recyclerNews.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val apiUrl = getString(R.string.root_url) + getString(R.string.LatestNews)
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
                            val newsArray = jsonObject.getJSONArray("news")

                            newsList.clear() // เคลียร์ข้อมูลก่อนเพิ่มรายการใหม่
                            for (i in 0 until newsArray.length()) {
                                val newsObj = newsArray.getJSONObject(i)
                                val newsID = newsObj.optString("NewsID", "")
                                val title = newsObj.optString("Title", "")
                                val sentiment = newsObj.optString("Sentiment", "")
                                val publishedDate = newsObj.optString("PublishedDate", "")

                                newsList.add(NewsList(newsID, title, publishedDate, sentiment))
                            }

                            newsAdapter = NewsAdapter(newsList) { selectedNews ->
                                val bundle = Bundle().apply {
                                    putString("NEWS_ID", selectedNews.NewsID)
                                }
                                findNavController().navigate(R.id.nav_invest, bundle)  // ตรวจสอบว่า `R.id.news_detail` มีการตั้งค่าไว้ใน navigation.xml หรือไม่
                            }

                            recyclerNews.adapter = newsAdapter

                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error parsing news data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch news data", Toast.LENGTH_SHORT).show()
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
