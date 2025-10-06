package com.pkm.sahabatgula.ui.settings.loghistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.databinding.ItemParentDateBinding

class ParentFoodHistoryAdapter(private val items: List<HistoryItem>?) :
    RecyclerView.Adapter<ParentFoodHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemParentDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParentDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = items?.get(position)
        with(holder.binding) {
            tvDate.text = historyItem?.date
            rvChild.layoutManager = LinearLayoutManager(root.context)
            rvChild.adapter = ChildFoodAdapter(historyItem?.foods?: emptyList())
        }
    }

    override fun getItemCount(): Int = items?.size?:0
}
