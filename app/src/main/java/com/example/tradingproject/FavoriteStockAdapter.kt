package com.example.tradingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteStockAdapter(
    private var favoriteStocks: List<FavoriteStock>, // รายการหุ้นโปรด
    private val onItemClick: (FavoriteStock) -> Unit // ฟังก์ชันเมื่อกดที่รายการ
) : RecyclerView.Adapter<FavoriteStockAdapter.FavoriteStockViewHolder>() {

    class FavoriteStockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stockSymbol: TextView = view.findViewById(R.id.stockName)
        val stockPrice: TextView = view.findViewById(R.id.stockPrice)
        val stockChange: TextView = view.findViewById(R.id.stockChange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteStockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_stock, parent, false)
        return FavoriteStockViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteStockViewHolder, position: Int) {
        val stock = favoriteStocks[position]
        holder.stockSymbol.text = stock.StockSymbol
        holder.stockPrice.text = stock.LastPrice
        holder.stockChange.text = stock.LastChange


        holder.itemView.setOnClickListener {
            onItemClick(stock) // เรียกเมื่อกดที่รายการ
        }
    }

    override fun getItemCount(): Int = favoriteStocks.size

    // อัปเดตรายการหุ้นโปรด
    fun updateData(newStocks: List<FavoriteStock>) {
        favoriteStocks = newStocks
        notifyDataSetChanged()
    }
}
