package com.example.tradingproject

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.example.tradingproject.R
import com.github.mikephil.charting.components.XAxis

class HomeFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private val stockList = mutableListOf<StockModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val stockChart = view.findViewById<LineChart>(R.id.stockChart)
        val greenColor = Color.parseColor("#4DC247")
        val colorUnder = Color.parseColor("#2E742B")

        val stockPrices = listOf(100, 102, 98, 105, 110, 108, 115, 120, 125)
        val entries = stockPrices.mapIndexed { index, price -> Entry(index.toFloat(), price.toFloat()) }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerStock)
        val recyclerFavoriteStock = view.findViewById<RecyclerView>(R.id.recyclerFavoriteStock)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)

        recyclerView.isNestedScrollingEnabled = false

        stockList.clear()
        stockList.addAll(
            listOf(
                StockModel("SET", "1,283.97", "1.06%", R.drawable.icon_flagus),
                StockModel("S&P 500", "6,068.50", "0.03%", R.drawable.icon_flagus),
                StockModel("NASDAQ", "19,643.85", "0.36%", R.drawable.icon_flagus),
                StockModel("DOW J", "44,593.65", "0.28%", R.drawable.icon_flagus),
                StockModel("BTC/USD", "96,094.13", "0.35%", R.drawable.icon_flagus),
                StockModel("Oil CL", "96,094.13", "0.35%", R.drawable.icon_flagus),
            )
        )

        stockAdapter = StockAdapter(stockList)
        recyclerFavoriteStock.adapter = stockAdapter
        recyclerView.adapter = stockAdapter


        val lineDataSet = LineDataSet(entries, "").apply {
            color = greenColor
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)

            // ✅ กำหนดสีถมใต้เส้นเป็นสีเขียว (ไม่ใช้ Gradient)
            fillColor = colorUnder
            fillAlpha = 150 // กำหนดให้สีทึบ (0 = โปร่งแสง, 255 = ทึบ)
        }

        // ตั้งค่ากราฟ

        val backgroundGraph = Color.parseColor("#25252A")
        stockChart.apply {
            data = LineData(lineDataSet)
            setBackgroundColor(backgroundGraph) // พื้นหลังสีดำ
            setNoDataText("Loading...") // ข้อความถ้าไม่มีข้อมูล
            description = Description().apply { text = "" } // ลบ Description
            legend.isEnabled = false // ไม่แสดง Legend (ชื่อกราฟ)

            // ปิดการโต้ตอบกับกราฟ (Disable Touch & Interaction)
            setTouchEnabled(false)  // ปิดการสัมผัส
            isDragEnabled = false   // ปิดการลาก
            setScaleEnabled(false)  // ปิดการ Zoom
            setPinchZoom(false)     // ปิด Pinch Zoom

            // ตั้งค่าแกน X ให้ซ่อน
            xAxis.apply {
                setDrawGridLines(false) // ซ่อนเส้น Grid
                setDrawLabels(false) // ไม่แสดงตัวเลขแกน X
                position = XAxis.XAxisPosition.BOTTOM
                axisLineColor = Color.TRANSPARENT // ซ่อนเส้นแกน X
            }

            // ตั้งค่าแกน Y ให้ซ่อน
            axisLeft.apply {
                setDrawGridLines(false) // ซ่อนเส้น Grid
                setDrawLabels(false) // ไม่แสดงตัวเลขแกน Y
                axisLineColor = Color.TRANSPARENT // ซ่อนเส้นแกน Y
            }

            axisRight.isEnabled = false // ปิดแกนขวา
            invalidate() // อัปเดตกำหนดค่าและแสดงผล
        }

        return view
    }
}
