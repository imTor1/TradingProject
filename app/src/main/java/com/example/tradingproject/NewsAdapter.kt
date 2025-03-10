package com.example.tradingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(
    private val newsList: List<NewsList>,
    private val onItemClick: (NewsList) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titlee)
        val dateTextView: TextView = view.findViewById(R.id.date)
        val sentimentTextView: TextView = view.findViewById(R.id.sentiment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_current, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.titleTextView.text = newsItem.Title
        holder.dateTextView.text = "วันที่: ${newsItem.PublishedDate}"
        holder.sentimentTextView.text = "Sentiment: ${newsItem.Sentiment}"

        holder.itemView.setOnClickListener {
            onItemClick(newsItem) // ส่งข้อมูลไปยัง Fragment เมื่อมีการคลิก
        }
    }

    override fun getItemCount() = newsList.size
}

