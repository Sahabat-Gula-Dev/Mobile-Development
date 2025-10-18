package com.pkm.sahabatgula.ui.settings.loghistory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.core.utils.dateFormatterHistory
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.databinding.ItemParentDateBinding

class ParentFoodHistoryAdapter(
    historyList: List<HistoryItem>?
) : RecyclerView.Adapter<ParentFoodHistoryAdapter.ParentViewHolder>() {

    private val items = historyList
        ?.filter { !it.foods.isNullOrEmpty() }
        ?.toMutableList() ?: mutableListOf()

    inner class ParentViewHolder(val binding: ItemParentDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val binding = ItemParentDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val historyItem = items[position]

        with(holder.binding) {
            tvDate.text = dateFormatterHistory(historyItem.date)
            lateinit var childAdapter: ChildFoodAdapter
            childAdapter = ChildFoodAdapter(historyItem.foods.toMutableList()) { pos, item ->
                childAdapter.collapseAllExcept(pos)
                item.isExpanded = !item.isExpanded
                childAdapter.updateItem(pos, item)
            }

            rvChild.layoutManager = LinearLayoutManager(root.context)
            rvChild.adapter = childAdapter
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<HistoryItem>) {
        items.clear()
        items.addAll(newData.filter { !it.foods.isNullOrEmpty() })
        notifyDataSetChanged()
    }
}
