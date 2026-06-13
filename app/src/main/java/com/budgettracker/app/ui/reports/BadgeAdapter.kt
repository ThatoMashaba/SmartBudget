package com.budgettracker.app.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.app.databinding.ItemBadgeBinding
import com.budgettracker.app.utils.Badge

class BadgeAdapter(
    private var badges: List<Badge>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    inner class BadgeViewHolder(val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]

        holder.binding.tvBadgeEmoji.text = badge.emoji
        holder.binding.tvBadgeTitle.text = badge.title
        holder.binding.tvBadgeDesc.text = badge.description

        if (badge.isEarned) {
            holder.binding.tvBadgeStatus.text = "✅"
            holder.binding.root.alpha = 1.0f
        } else {
            holder.binding.tvBadgeStatus.text = "🔒"
            holder.binding.root.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int = badges.size

    fun updateList(newList: List<Badge>) {
        badges = newList
        notifyDataSetChanged()
    }
}