package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.setSize
import com.pkm.sahabatgula.data.remote.model.ActivitiesDataItem
import com.pkm.sahabatgula.databinding.ItemCardCustomFoodBinding

class ActivityPagingAdapter(
    private val onSelectClick: (ActivitiesDataItem) -> Unit,
    private val onExpandClick: (ActivitiesDataItem) -> Unit
) : PagingDataAdapter<ActivitiesDataItem, ActivityPagingAdapter.ActivityViewHolder>(ACTIVITY_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemCardCustomFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding, onSelectClick, onExpandClick, this)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class ActivityViewHolder(
        private val binding: ItemCardCustomFoodBinding,
        private val onSelectClick: (ActivitiesDataItem) -> Unit,
        private val onExpandClick: (ActivitiesDataItem) -> Unit,
        private val adapter: ActivityPagingAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentActivity: ActivitiesDataItem? = null

        init {
            binding.icPlusAddFood.setOnClickListener {
                currentActivity?.let {
                    onSelectClick(it)
                    adapter.notifyItemChanged(bindingAdapterPosition)
                }
            }
            binding.icArrowRight.setOnClickListener {
                currentActivity?.let {
                    onExpandClick(it)
                    adapter.notifyItemChanged(bindingAdapterPosition)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(activity: ActivitiesDataItem) {
            currentActivity = activity

            val title = "${activity.name} ${activity.duration} ${activity.durationUnit}"
            binding.tvTitleCustomFoodCard.text = title
            binding.tvTitleCustomFoodExpand.text = title
            binding.tvFoodCalories.text = "${activity.caloriesBurned} kkal"
            binding.tvFoodCaloriesOnExpand.setTextColor("#C80000".toColorInt())

            if (activity.isSelected) {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_check_activity_yellow)
            } else {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_add_plus_activity_yellow)
            }

            if (activity.isExpanded) {
                binding.tvTitleCustomFoodExpand.visibility = View.VISIBLE
                binding.tvTitleCustomFoodCard.visibility = View.GONE
                binding.expandedView.visibility = View.VISIBLE
                binding.tvFoodCalories.visibility = View.GONE
                binding.icPlusAddFood.visibility = View.GONE

                binding.icArrowRight.setImageResource(R.drawable.arrow_down_custom_food)
                binding.icArrowRight.setSize(36)

                binding.tvFoodCaloriesOnExpand.text = "${activity.caloriesBurned} kkal"
                binding.tvDescFood.text = activity.description
                Glide.with(itemView.context)
                    .load(activity.photoUrl)
                    .placeholder(R.drawable.image_placeholder_color)
                    .into(binding.imgFood)
            } else {
                binding.tvTitleCustomFoodExpand.visibility = View.GONE
                binding.tvTitleCustomFoodCard.visibility = View.VISIBLE
                binding.expandedView.visibility = View.GONE
                binding.tvFoodCalories.visibility = View.VISIBLE
                binding.icPlusAddFood.visibility = View.VISIBLE

                if (activity.isSelected) {
                    binding.icArrowRight.setImageResource(R.drawable.ic_calories)
                    binding.icArrowRight.setSize(24)
                } else {
                    binding.icArrowRight.setImageResource(R.drawable.arrow_right_custom_food_svg)
                    binding.icArrowRight.setSize(36)
                }
            }
        }
    }

    companion object {
        private val ACTIVITY_COMPARATOR = object : DiffUtil.ItemCallback<ActivitiesDataItem>() {
            override fun areItemsTheSame(oldItem: ActivitiesDataItem, newItem: ActivitiesDataItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ActivitiesDataItem, newItem: ActivitiesDataItem): Boolean =
                oldItem == newItem
        }
    }
}
