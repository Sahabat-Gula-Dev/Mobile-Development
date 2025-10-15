package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.data.remote.model.FoodsItem
import com.pkm.sahabatgula.databinding.ItemCardFoodBinding

class ResultScanAdapter(private val onItemClicked: (FoodsItem) -> Unit) :
    ListAdapter<FoodsItem, ResultScanAdapter.FoodViewHolder>(DIFF_CALLBACK){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodViewHolder {
        val binding = ItemCardFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FoodViewHolder,
        position: Int
    ) {
        val food = getItem(position)
        holder.bind(food)
        holder.itemView.setOnClickListener {onItemClicked(food)}
    }

    class FoodViewHolder(private val binding: ItemCardFoodBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(food: FoodsItem) {
            val servingUnit = food.servingUnit
            val foodServingUnit = servingUnit?.replaceFirstChar { it.uppercase() }
            binding.tvFoodName.text = "${food.name} ${food.servingSize} ${foodServingUnit} ${food.weightSize} ${food.weightUnit}"
            binding.tvCalories.text = "${food.calories} kkal"
            binding.tvFoodDesc.text = food.description
            Glide.with(binding.root.context)
                .load(food.photoUrl)
                .into(binding.imgFood)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FoodsItem>() {
            override fun areItemsTheSame(oldItem: FoodsItem, newItem: FoodsItem): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: FoodsItem, newItem: FoodsItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}