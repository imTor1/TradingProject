package com.example.tradingproject

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class HomeFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private lateinit var favoriteAdapter: StockFavoriteAdapter
    private val stockList = mutableListOf<StockModel>()
    private val favoriteStockList = mutableListOf<StockModel>()


    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        FavoriteStock(view)
        RecommenStock(view)
        autoScrollRecyclerView(view)







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
                StockModel("SET", "1,283.97", "1.06%", R.drawable.icon_flagus),
                StockModel("S&P 500", "6,068.50", "0.03%", R.drawable.icon_flagus),
                StockModel("NASDAQ", "19,643.85", "0.36%", R.drawable.icon_flagus),
                StockModel("DOW J", "44,593.65", "0.28%", R.drawable.icon_flagus),
                StockModel("SET", "1,283.97", "1.06%", R.drawable.icon_flagus),
                StockModel("S&P 500", "6,068.50", "0.03%", R.drawable.icon_flagus),
                StockModel("NASDAQ", "19,643.85", "0.36%", R.drawable.icon_flagus),
                StockModel("DOW J", "44,593.65", "0.28%", R.drawable.icon_flagus)
            )
        )
        stockAdapter = StockAdapter(stockList)
        recyclerRecommended.adapter = stockAdapter
    }

    private fun FavoriteStock(view: View) {
        val recyclerFavoriteStock = view.findViewById<RecyclerView>(R.id.recyclerFavoriteStock)

        recyclerFavoriteStock.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        if (favoriteStockList.isEmpty()) {
            favoriteStockList.addAll(
                listOf(
                    StockModel("AAPL", "150.75 USD", "1.5%", R.drawable.icon_flagus),
                    StockModel("NVDA", "210.25 USD", "-0.7%", R.drawable.icon_flagus),
                    StockModel("TSLA", "720.50 USD", "3.2%", R.drawable.icon_flagus),
                    StockModel("GOOGL", "2800.40 USD", "+0.9%", R.drawable.icon_flagus)
                )
            )
        }
        val favoriteAdapter = StockFavoriteAdapter(favoriteStockList)
        recyclerFavoriteStock.adapter = favoriteAdapter
    }

    private fun autoScrollRecyclerView(view: View) {
        val recyclerStock = view.findViewById<RecyclerView>(R.id.recyclerStock)
        val handler = Handler(Looper.getMainLooper())

        val scrollDistance = 5   // ✅ กำหนดระยะเลื่อน (ยิ่งน้อยยิ่งช้า)
        val scrollInterval = 50L // ✅ ค่ามิลลิวินาที (ยิ่งสูงยิ่งช้า)

        val runnable = object : Runnable {
            override fun run() {
                val layoutManager = recyclerStock.layoutManager as LinearLayoutManager

                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1) {
                    recyclerStock.scrollToPosition(0) // ✅ กลับไปเริ่มที่รายการแรก
                } else {
                    recyclerStock.smoothScrollBy(scrollDistance, 0) // ✅ เลื่อนไปด้านขวาแบบ Smooth
                }

                handler.postDelayed(this, scrollInterval) // ✅ เรียกซ้ำทุก 50ms
            }
        }
        handler.post(runnable) // ✅ เริ่มการ Scroll
    }




}

