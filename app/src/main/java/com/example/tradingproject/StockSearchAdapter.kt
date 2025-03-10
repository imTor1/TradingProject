package com.example.tradingproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockSearchAdapter(
    private var stockList: List<StockSearch>,
    private val onItemClick: (StockSearch) -> Unit
) : RecyclerView.Adapter<StockSearchAdapter.StockViewHolder>() {

    class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stockSymbol: TextView = itemView.findViewById(R.id.SearchName)
        val companyName: TextView = itemView.findViewById(R.id.SearchCompanyStock)
        val flagimg: ImageView = itemView.findViewById(R.id.flagimgSearch)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.stockSymbol.text = stock.StockSymbol
        holder.companyName.text = stock.CompanyName
        holder.flagimg.setImageResource(stock.FlagIcon)

        // เพิ่ม Event Listener เมื่อคลิกที่รายการ
        holder.itemView.setOnClickListener {
            onItemClick(stock)
        }
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    fun updateData(newList: List<StockSearch>) {
        stockList = newList
        notifyDataSetChanged()
    }
}

