package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.databinding.ItemCardFoodBinding

class FoodPagingAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : PagingDataAdapter<FoodItem, FoodPagingAdapter.FoodViewHolder>(FOOD_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemCardFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = getItem(position)
        if (foodItem != null) {
            holder.bind(foodItem)
        }
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val food: FoodItem? = getItem(position)
                onItemClick(food?: return@setOnClickListener)
            }
        }
    }

    class FoodViewHolder(
        private val binding: ItemCardFoodBinding,
        private val onItemClick: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: FoodItem) {
            binding.tvFoodName.text = food.name
            binding.tvFoodDesc.text = food.description
            binding.tvCalories.text = "${food.calories.toInt()} kkal"
            Glide.with(itemView.context)
                .load(food.photoUrl)
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imgFood)


            binding.root.setOnClickListener { onItemClick(food) }
        }
    }

    companion object {
        private val FOOD_COMPARATOR = object : DiffUtil.ItemCallback<FoodItem>() {
            override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean =
                oldItem == newItem
        }
    }
}