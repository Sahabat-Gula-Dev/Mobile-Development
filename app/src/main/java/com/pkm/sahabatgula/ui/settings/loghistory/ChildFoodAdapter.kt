package com.pkm.sahabatgula.ui.settings.loghistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.remote.model.FoodLog
import com.pkm.sahabatgula.databinding.ItemChildFoodBinding

class ChildFoodAdapter(
    private val items: MutableList<FoodLog>,
    private val onExpand: (position: Int, item: FoodLog) -> Unit
) : RecyclerView.Adapter<ChildFoodAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChildFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FoodLog) {
            with(binding) {
                tvTitleCustomFoodCard.text = "${item.name} ${item.servingSize} ${item.servingUnit} ${item.weightSize} ${item.weightUnit}"
                tvTitleCustomFoodExpand.text = "${item.name} ${item.servingSize} ${item.servingUnit} ${item.weightSize} ${item.weightUnit}"
                tvFoodCalories.text = "${item.calories.toInt()} kkal"
                tvDescFood.text = item.description ?: ""
                tvTitleCustomFoodCard.visibility = if (item.isExpanded) View.GONE else View.VISIBLE
                tvTitleCustomFoodExpand.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

                Glide.with(root.context)
                    .load(item.photoUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(imgFood)

                expandedView.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                icArrowRight.animate().rotation(if (item.isExpanded) 90f else 0f).setDuration(200).start()

                root.setOnClickListener {
                    onExpand(bindingAdapterPosition, item)
                }
                tvFoodCalories.visibility = if (item.isExpanded) View.GONE else View.VISIBLE

                tvFoodCaloriesOnExpand.text = "${item.calories.toInt()} kkal"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChildFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateItem(position: Int, newItem: FoodLog) {
        items[position] = newItem
        notifyItemChanged(position)
    }

    fun collapseAllExcept(exceptPos: Int) {
        for (i in items.indices) {
            if (i != exceptPos && items[i].isExpanded) {
                items[i].isExpanded = false
                notifyItemChanged(i)
            }
        }
    }
}
