package com.example.tradingproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InvestFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_invest, container, false)
        RecommenStock(view)
        val searchbar = view.findViewById<TextView>(R.id.search_bar)

        searchbar.setOnClickListener {
            findNavController().navigate(R.id.search)
        }

        return view
    }

    private fun RecommenStock(view: View){
        val recyclerRecommended = view.findViewById<RecyclerView>(R.id.recyclerStock)
        recyclerRecommended.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecommended.setHasFixedSize(true)
        recyclerRecommended.isNestedScrollingEnabled = false
        stockList.clear()
        stockList.addAll(
            listOf(
                StockModel("SET", "1,283.97", "1.06%", R.drawable.icon_flagth),
                StockModel("S&P 500", "6,068.50", "0.03%", R.drawable.icon_flagus),
                StockModel("NASDAQ", "19,643.85", "0.36%", R.drawable.icon_flagus),
                StockModel("DOW J", "44,593.65", "0.28%", R.drawable.icon_flagth),
                StockModel("FTSE 100", "7,543.21", "0.12%", R.drawable.icon_flagus),
                StockModel("NIKKEI 225", "32,540.30", "0.78%", R.drawable.icon_flagus),
                StockModel("HANG SENG", "19,800.12", "-0.45%", R.drawable.icon_flagth),
                StockModel("DAX", "15,789.64", "0.09%", R.drawable.icon_flagth),
                StockModel("CAC 40", "7,123.85", "0.18%", R.drawable.icon_flagus),
                StockModel("BSE Sensex", "60,145.50", "0.35%", R.drawable.icon_flagus),
                StockModel("NIFTY 50", "18,245.65", "0.27%", R.drawable.icon_flagus),
                StockModel("Russell 2000", "2,145.30", "0.15%", R.drawable.icon_flagus),
                StockModel("ASX 200", "7,350.25", "-0.22%", R.drawable.icon_flagth),
                StockModel("KOSPI", "2,845.40", "0.41%", R.drawable.icon_flagus),
                StockModel("Shanghai Composite", "3,482.67", "-0.18%", R.drawable.icon_flagth),
                StockModel("TSE", "29,874.80", "0.24%", R.drawable.icon_flagus),
                StockModel("BMV IPC", "51,789.23", "0.12%", R.drawable.icon_flagus),
                StockModel("TSX", "21,145.20", "0.30%", R.drawable.icon_flagth),
                StockModel("Dow Transports", "14,632.50", "0.21%", R.drawable.icon_flagus),
                StockModel("MSCI World", "2,345.12", "0.08%", R.drawable.icon_flagth),
                StockModel("S&P Asia 50", "5,240.50", "-0.11%", R.drawable.icon_flagus),
                StockModel("EURO STOXX 50", "4,155.40", "0.16%", R.drawable.icon_flagth),
            )
        )
        stockAdapter = StockAdapter(stockList)
        recyclerRecommended.adapter = stockAdapter
    }
}