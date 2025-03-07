package com.example.tradingproject

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import androidx.navigation.fragment.findNavController



class HomeFragment : Fragment() {
    private lateinit var stockAdapter: StockAdapter
    private val favoriteAdapter by lazy { StockFavoriteAdapter(favoriteStockList) }
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
        val searchbar = view.findViewById<TextView>(R.id.search_bar)
        val addfavorite = view.findViewById<ImageView>(R.id.addfavorite)
        val newspage = view.findViewById<ImageView>(R.id.enter_newspage)

        newspage.setOnClickListener {
            findNavController().navigate(R.id.nav_news)
        }

        addfavorite.setOnClickListener {
            findNavController().navigate(R.id.search)
        }


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
        recyclerFavoriteStock.adapter = favoriteAdapter  // ใช้ instance ที่ประกาศไว้แล้ว
        enableSwipeToShowDelete(recyclerFavoriteStock)
    }



    private fun autoScrollRecyclerView(view: View) {
        val recyclerStock = view.findViewById<RecyclerView>(R.id.recyclerStock)
        val handler = Handler(Looper.getMainLooper())

        val scrollDistance = 5   // ระยะเลื่อน (ยิ่งน้อยยิ่งช้า)
        val scrollInterval = 50L // ค่ามิลลิวินาที (ยิ่งสูงยิ่งช้า)
        val restartDelay = 1000L // หยุด 1 วินาทีก่อนกลับมา Scroll

        var isUserScrolling = false

        val runnable = object : Runnable {
            override fun run() {
                if (!isUserScrolling) {
                    val layoutManager = recyclerStock.layoutManager as LinearLayoutManager

                    if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1) {
                        recyclerStock.scrollToPosition(0)
                    } else {
                        recyclerStock.smoothScrollBy(scrollDistance, 0)
                    }

                    handler.postDelayed(this, scrollInterval)
                }
            }
        }

        handler.post(runnable)

        recyclerStock.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        isUserScrolling = true
                        handler.removeCallbacks(runnable)
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        handler.postDelayed({
                            isUserScrolling = false
                            handler.post(runnable)
                        }, restartDelay)
                    }
                }
            }
        })
    }


    private fun enableSwipeToShowDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                favoriteAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val deleteButton = itemView.findViewById<Button>(R.id.deleteButton)
                val mainLayout = itemView.findViewById<LinearLayout>(R.id.mainLayout)

                // จำกัดระยะเลื่อนเท่าความกว้างปุ่ม
                val deleteButtonWidth = (deleteButton?.width ?: 100).toFloat()
                val clampedDX = dX.coerceAtMost(0f).coerceAtLeast(-deleteButtonWidth)

                if (dX < 0) {
                    // ถ้าลากไปทางซ้าย
                    deleteButton?.visibility = View.VISIBLE
                    mainLayout.translationX = clampedDX

                    // กดปุ่ม Delete จึงค่อยลบจริง
                    deleteButton?.setOnClickListener {
                        val position = viewHolder.adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            favoriteStockList.removeAt(position)
                            favoriteAdapter.notifyItemRemoved(position)
                        }
                    }
                } else {
                    // ถ้าไม่ได้ลากซ้าย ก็ซ่อนปุ่ม
                    deleteButton?.visibility = View.GONE
                    mainLayout.translationX = 0f
                }

                super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }






}

