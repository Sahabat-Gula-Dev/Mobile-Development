package com.pkm.sahabatgula.ui.settings.loghistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.core.utils.dateFormatterHistory
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.databinding.ItemParentDateBinding

class ParentActivityHistoryAdapter(
    historyList: List<HistoryItem>?
) : RecyclerView.Adapter<ParentActivityHistoryAdapter.ParentViewHolder>() {

    private val filteredList = historyList?.filter { !it.activities.isNullOrEmpty() } ?: emptyList()

    inner class ParentViewHolder(val binding: ItemParentDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val binding = ItemParentDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val historyItem = filteredList[position]

        with(holder.binding) {
            tvDate.text = dateFormatterHistory(historyItem.date)
            lateinit var childAdapter: ChildActivityAdapter
            childAdapter = ChildActivityAdapter(historyItem.activities.toMutableList()) { pos, item ->
                childAdapter.collapseAllExcept(pos)
                item.isExpanded = !item.isExpanded
                childAdapter.updateItem(pos, item)
            }

            rvChild.layoutManager = LinearLayoutManager(root.context)
            rvChild.adapter = childAdapter
        }
    }

    override fun getItemCount(): Int = filteredList.size
}
