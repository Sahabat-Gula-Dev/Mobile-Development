package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.setSize
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.databinding.ItemCardCustomFoodBinding

class CustomFoodPagingAdapter (
    private val onSelectClick: (FoodItem) -> Unit,
    private val onExpandClick: (FoodItem) -> Unit
): PagingDataAdapter<FoodItem, CustomFoodPagingAdapter.FoodCustomViewHolder>(FOOD_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):FoodCustomViewHolder {
        val binding = ItemCardCustomFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodCustomViewHolder(binding, onSelectClick, onExpandClick)
    }

    override fun onBindViewHolder(
        holder: FoodCustomViewHolder,
        position: Int
    ) {
        val foodItem = getItem(position)
        if (foodItem != null) {
            holder.bind(foodItem)
        }
    }

    class FoodCustomViewHolder(
        private val binding: ItemCardCustomFoodBinding,
        private val onSelectClick: (FoodItem) -> Unit,
        private val onExpandClick: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentFood: FoodItem? = null

        init {
            binding.icPlusAddFood.setOnClickListener {
                currentFood?.let { onSelectClick(it) }
            }

            val expandListener = View.OnClickListener {
                if (currentFood?.isSelected == false) {
                    currentFood?.let { onExpandClick(it) }
                }
            }

            binding.icArrowRight.setOnClickListener(expandListener)
        }

        fun bind(food: FoodItem) {
            currentFood = food

            binding.tvTitleCustomFoodCard.text = "${food.name} ${food.servingSize} ${food.servingUnit} ${food.weightSize} ${food.weightUnit}"
            binding.tvFoodCalories.text = "${food.calories.toInt()} kkal"
            binding.tvTitleCustomFoodExpand.text = "${food.name} ${food.servingSize} ${food.servingUnit} ${food.weightSize} ${food.weightUnit}"

            if (food.isSelected) {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_checked)
            } else {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_plus_add_food)
            }

            if (food.isExpanded) {
                binding.expandedView.visibility = View.VISIBLE
                binding.tvFoodCalories.visibility = View.GONE
                binding.icPlusAddFood.visibility = View.GONE
                binding.tvTitleCustomFoodExpand.visibility = View.VISIBLE
                binding.tvTitleCustomFoodCard.visibility = View.GONE

                binding.icArrowRight.setImageResource(R.drawable.arrow_down_custom_food)
                binding.icArrowRight.setSize(36)

                // Isi data untuk view di dalam expanded_view
                binding.tvFoodCaloriesOnExpand.text = "${food.calories.toInt()} kkal"
                binding.tvDescFood.text = food.description
                Glide.with(itemView.context)
                    .load(food.photoUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.imgFood)

            } else {
                binding.tvTitleCustomFoodExpand.visibility = View.GONE
                binding.tvTitleCustomFoodCard.visibility = View.VISIBLE
                binding.expandedView.visibility = View.GONE
                binding.tvFoodCalories.visibility = View.VISIBLE
                binding.icPlusAddFood.visibility = View.VISIBLE

                if (food.isSelected) {
                    binding.icArrowRight.setImageResource(R.drawable.ic_food_salad)
                    binding.icArrowRight.setSize(24)
                } else {
                    binding.icArrowRight.setImageResource(R.drawable.arrow_right_custom_food_svg)
                    binding.icArrowRight.setSize(36)
                }
            }
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