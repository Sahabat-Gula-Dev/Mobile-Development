package com.pkm.sahabatgula.ui.auth.register.inputdatauser.height

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.ItemRulerBinding

class RulerAdapter(private val minValue: Int, private val maxValue: Int) : RecyclerView.Adapter<RulerAdapter.RulerViewHolder>() {

    inner class RulerViewHolder(val binding: ItemRulerBinding) : RecyclerView.ViewHolder(binding.root) {
        val tick: View = binding.tick
        val label: TextView = binding.label
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RulerViewHolder {

        val binding = ItemRulerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RulerViewHolder(binding)
    }

    override fun getItemCount(): Int = maxValue - minValue + 1

    override fun onBindViewHolder(holder: RulerViewHolder, position: Int) {
        val value = minValue + position

        if (value % 10 == 0) {
            holder.label.text = value.toString()
            holder.label.visibility = View.VISIBLE

            // garis lebih tinggi dan lebih tebal
            holder.tick.layoutParams.height = 130
            holder.tick.layoutParams.width = 8
            holder.tick.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_onSurfaceVariant)
            )

        } else if (value % 5 == 0) {
            holder.label.text = value.toString()
            holder.label.visibility = View.GONE

            // garis lebih tinggi dan lebih tebal
            holder.tick.layoutParams.height = 130
            holder.tick.layoutParams.width = 8
            holder.tick.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_onSurfaceVariant)
            )

        }
        else {
            holder.label.visibility = View.GONE

            // garis normal
            holder.tick.layoutParams.height = 80
            holder.tick.layoutParams.width = 4

            holder.tick.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.md_theme_outline)
            )
        }

        holder.tick.requestLayout()
    }

}