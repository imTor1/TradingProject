import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tradingproject.R
import com.example.tradingproject.StockModel

class StockAdapter(
    private var stockList: MutableList<StockModel>,
    private val onItemClick: (StockModel) -> Unit // ✅ เพิ่ม callback เมื่อกดหุ้น
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stockName: TextView = view.findViewById(R.id.stockName)
        val stockPrice: TextView = view.findViewById(R.id.stockPrice)
        val stockChange: TextView = view.findViewById(R.id.stockChange)
        val flagimg: ImageView = view.findViewById(R.id.flagimg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.stockName.text = stock.StockSymbol
        holder.stockPrice.text = stock.ClosePrice
        holder.stockChange.text = stock.ChangePercentage
        holder.flagimg.setImageResource(stock.FlagIcon)

        holder.itemView.setOnClickListener {
            onItemClick(stock)
        }
    }

    override fun getItemCount(): Int = stockList.size
}
