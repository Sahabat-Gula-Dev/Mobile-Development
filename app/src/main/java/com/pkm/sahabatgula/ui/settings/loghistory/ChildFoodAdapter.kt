package com.pkm.sahabatgula.ui.settings.loghistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.remote.model.FoodLog
import com.pkm.sahabatgula.databinding.ItemChildFoodBinding

class ChildFoodAdapter(private val items: List<FoodLog>) :
    RecyclerView.Adapter<ChildFoodAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChildFoodBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChildFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvTitleCustomFoodCard.text = item.name
            tvFoodCalories.text = "${item.calories} kkal"
            tvDescFood.text = item.description
            Glide.with(root.context)
                .load(item.photoUrl)
                .placeholder(R.drawable.image_placeholder)
                .into(imgFood)
        }
    }

    override fun getItemCount() = items.size
}
