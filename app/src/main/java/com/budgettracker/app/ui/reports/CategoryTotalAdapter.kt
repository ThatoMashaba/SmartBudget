package com.budgettracker.app.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.app.data.dao.CategoryTotal
import com.budgettracker.app.databinding.ItemCategoryTotalBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryTotalAdapter(
    private var items: List<CategoryTotal>
) : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    // Track grand total to calculate percentages
    private var grandTotal: Double = 0.0

    inner class ViewHolder(val binding: ItemCategoryTotalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTotalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Set category name
        holder.binding.tvCategoryTotalName.text = item.categoryName

        // Set formatted amount
        holder.binding.tvCategoryTotalAmount.text = currencyFormat.format(item.total)

        // Calculate and show percentage
        val percentage = if (grandTotal > 0) {
            ((item.total / grandTotal) * 100).toInt()
        } else {
            0
        }
        holder.binding.progressCategory.progress = percentage
        holder.binding.tvCategoryPercentage.text = "$percentage% of total spending"
    }

    override fun getItemCount(): Int = items.size

    // Update list and recalculate grand total
    fun updateList(newItems: List<CategoryTotal>) {
        items = newItems
        grandTotal = newItems.sumOf { it.total }
        notifyDataSetChanged()
    }
}