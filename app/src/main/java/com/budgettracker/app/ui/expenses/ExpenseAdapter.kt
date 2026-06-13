package com.budgettracker.app.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.app.data.entities.Expense
import com.budgettracker.app.databinding.ItemExpenseBinding
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.binding.tvExpenseDate.text = "📅 ${expense.date}"
        holder.binding.tvExpenseDesc.text = expense.description
        holder.binding.tvExpenseTime.text = "⏰ ${expense.startTime} — ${expense.endTime}"
        holder.binding.tvExpenseAmount.text = currencyFormat.format(expense.amount)

        if (expense.photoPath != null) {
            holder.binding.ivExpensePhoto.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(expense.photoPath)
                .centerCrop()
                .into(holder.binding.ivExpensePhoto)
        } else {
            holder.binding.ivExpensePhoto.visibility = View.GONE
        }

        // Tap to edit
        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateList(newList: List<Expense>) {
        expenses = newList
        notifyDataSetChanged()
    }

    fun getTotal(): Double = expenses.sumOf { it.amount }
}