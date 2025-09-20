package com.pkm.sahabatgula.ui.auth.register.inputdatauser.age

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.ItemAgeBinding

class AgePickerAdapter: RecyclerView.Adapter<AgePickerAdapter.AgeViewHolder>() {

    private val ageList = (10..98).toList()
    var selectedPosition = -1

    inner class AgeViewHolder(private val binding: ItemAgeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(age: Int, isSelected: Boolean) {
            binding.tvAge.text = age.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgeViewHolder {
        val binding = ItemAgeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AgeViewHolder, position: Int) {
        holder.bind(ageList[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = ageList.size

    fun getAgeAtPosition(position: Int): Int = ageList[position]
    fun getPositionForAge(age: Int): Int = ageList.indexOf(age)
}