package com.example.tradingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockFavoriteAdapter(
    private val stockList: MutableList<StockModel>
) : RecyclerView.Adapter<StockFavoriteAdapter.StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.bind(stock)
    }

    override fun getItemCount(): Int = stockList.size

    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stockName: TextView = itemView.findViewById(R.id.stockName)
        private val stockPrice: TextView = itemView.findViewById(R.id.stockPrice)
        private val stockChange: TextView = itemView.findViewById(R.id.stockChange)

        fun bind(stock: StockModel) {
            stockName.text = stock.name
            stockPrice.text = stock.price
            stockChange.text = stock.change
        }
    }
}
