package com.pkm.sahabatgula.ui.auth.register.inputdatauser.heavy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.ItemWeightBinding

class HeavyAdapter: RecyclerView.Adapter<HeavyAdapter.WeightViewHolder>() {

    private val weightList = (10..300).toList()
    var selectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeightViewHolder {
        val binding = ItemWeightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WeightViewHolder,
        position: Int
    ) {
        holder.bind(weightList[position], position == selectedPosition)
    }

    override fun getItemCount(): Int {
        return weightList.size
    }

    class WeightViewHolder(private val binding: ItemWeightBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(weight: Int, isSelected: Boolean) {
            binding.tvWeight.text = weight.toString()
            if (isSelected) {
                binding.tvWeight.setTextColor(ContextCompat.getColor(binding.root.context, R.color.number_picker_v1)) // atau pakai warna utama
                binding.tvWeight.textSize = 32f
            } else {
                binding.tvWeight.setTextColor(ContextCompat.getColor(binding.root.context, R.color.number_picker_v2))
                binding.tvWeight.textSize = 18f
            }
        }
    }

    fun getWeightAtPosition(position: Int):Int {
        return weightList[position]
    }

    fun getPositionForWeight(weight: Int): Int {
        return if (weight in 10..300) {
            weight - 10  // karena weightList dimulai dari 10, jadi age 10 = position 0, age 25 = position 15
        } else {
            - 1
        }
    }
}