package com.pkm.sahabatgula.ui.settings.loghistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.databinding.ItemParentDateBinding

class ParentFoodHistoryAdapter(
    private val historyList: List<HistoryItem>?
) : RecyclerView.Adapter<ParentFoodHistoryAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(val binding: ItemParentDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val binding = ItemParentDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val historyItem = historyList?.get(position)
        with(holder.binding) {
            tvDate.text = historyItem?.date

            var childAdapter: ChildFoodAdapter? = null

            childAdapter = ChildFoodAdapter(historyItem?.foods?.toMutableList()!!) { pos, item ->
                childAdapter?.collapseAllExcept(pos)
                item.isExpanded = !item.isExpanded
                childAdapter?.updateItem(pos, item)
            }

            rvChild.layoutManager = LinearLayoutManager(root.context)
            rvChild.adapter = childAdapter

        }
    }

    override fun getItemCount(): Int = historyList!!.size
}
