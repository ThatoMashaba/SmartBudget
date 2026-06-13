package com.budgettracker.app.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.app.data.entities.Category
import com.budgettracker.app.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categories: List<Category>,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Set the category name
        holder.binding.tvCategoryName.text = category.name

        // Delete button click
        holder.binding.btnDeleteCategory.setOnClickListener {
            onDelete(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    // Call this to refresh the list
    fun updateList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }
}