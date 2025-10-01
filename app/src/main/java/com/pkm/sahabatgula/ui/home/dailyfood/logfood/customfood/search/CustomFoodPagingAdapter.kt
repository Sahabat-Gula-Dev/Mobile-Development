package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood.search

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
            // Mengatur listener di init() lebih efisien karena hanya dijalankan sekali
            binding.icPlusAddFood.setOnClickListener {
                currentFood?.let { onSelectClick(it) }
            }
            binding.icArrowRight.setOnClickListener {
                currentFood?.let { onExpandClick(it) }
            }
            binding.root.setOnClickListener {
                currentFood?.let { onExpandClick(it) }
            }
        }

        fun bind(food: FoodItem) {
            currentFood = food

            // --- Mengisi data untuk state normal (collapsed) ---
            binding.tvTitleCustomFoodCard.text = "${food.name} ${food.servingSize} ${food.servingUnit} ${food.weightSize} ${food.weightUnit}"
            binding.tvFoodCalories.text = "${food.calories.toInt()} kkal"

            // --- Mengelola state terpilih (selected) ---
            if (food.isSelected) {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_checked)
            } else {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_plus_add_food)
            }

            if (food.isExpanded) {
                // Prioritas 1: Jika item di-expand, panah selalu ke bawah.
                binding.icArrowRight.setImageResource(R.drawable.ic_arrow_down)
            } else {
                // Prioritas 2: Jika tidak di-expand, baru cek status selection.
                if (food.isSelected) {
                    // Ini adalah kondisi yang Anda inginkan
                    binding.icArrowRight.setImageResource(R.drawable.ic_food_salad)
                    binding.icArrowRight.setSize(32)
                } else {
                    // State default: tidak di-expand dan tidak di-select.
                    binding.icArrowRight.setImageResource(R.drawable.ic_arrow_right)
                    binding.icArrowRight.setSize(18)
                }
            }

            // --- Mengelola state expand/collapse ---
            if (food.isExpanded) {
                // Tampilkan grup expanded_view
                binding.expandedView.visibility = View.VISIBLE
                // Ubah ikon panah
                binding.icArrowRight.setImageResource(R.drawable.ic_arrow_down)
                binding.icArrowRight.setSize(18)
                binding.tvFoodCalories.visibility = View.GONE

                // Isi data untuk view di dalam expanded_view
                binding.tvFoodCaloriesOnExpand.text = "${food.calories.toInt()} kkal"
                binding.tvDescFood.text = food.description
                Glide.with(itemView.context)
                    .load(food.photoUrl)
                    .placeholder(R.drawable.image_placeholder) // Ganti dengan placeholder Anda
                    .into(binding.imgFood)

            } else {
                // Sembunyikan grup expanded_view
                binding.expandedView.visibility = View.GONE
                // Tampilkan kembali kalori versi collapsed
                binding.tvFoodCalories.visibility = View.VISIBLE
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