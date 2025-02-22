package com.example.tradingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(private var stockList: MutableList<StockModel>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stockName: TextView = view.findViewById(R.id.stockName)
        val stockPrice: TextView = view.findViewById(R.id.stockPrice)
        val stockChange: TextView = view.findViewById(R.id.stockChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.stockName.text = stock.name
        holder.stockPrice.text = stock.price
        holder.stockChange.text = stock.change
    }

    override fun getItemCount(): Int = stockList.size
}

