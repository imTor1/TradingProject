package com.example.tradingproject

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val stockChart = view.findViewById<LineChart>(R.id.stockChart)
        val greenColor = Color.parseColor("#4DC247")
        val colorUnder = Color.parseColor("#2E742B")



        // สร้างข้อมูลราคาหุ้นสมมุติ
        val stockPrices = listOf(100, 102, 98, 105, 110, 108, 115, 120, 125)
        val entries = stockPrices.mapIndexed { index, price -> Entry(index.toFloat(), price.toFloat()) }

        // สร้าง DataSet สำหรับเส้นกราฟ
        val lineDataSet = LineDataSet(entries, "").apply {
            color = greenColor // ✅ สีเส้นกราฟ
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
