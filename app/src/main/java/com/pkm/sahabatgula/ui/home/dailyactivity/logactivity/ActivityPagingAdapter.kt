package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
): PagingDataAdapter<ActivitiesDataItem, ActivityPagingAdapter.ActivityViewHolder>(ACTIVITY_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivityPagingAdapter.ActivityViewHolder {
        val binding = ItemCardCustomFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding, onSelectClick, onExpandClick)
    }

    override fun onBindViewHolder(
        holder: ActivityPagingAdapter.ActivityViewHolder,
        position: Int
    ) {
        val activity = getItem(position)
        if (activity != null) {
            holder.bind(activity)
        }
    }

    class ActivityViewHolder(
        private val binding: ItemCardCustomFoodBinding,
        private val onSelectClick: (ActivitiesDataItem) -> Unit,
        private val onExpandClick: (ActivitiesDataItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentActivity: ActivitiesDataItem? = null

        init {
            binding.icPlusAddFood.setOnClickListener {
                currentActivity?.let { onSelectClick(it) }
            }

            val expandListener = View.OnClickListener {
                if (currentActivity?.isSelected == false) {
                    currentActivity?.let { onExpandClick(it) }
                }
            }

            binding.icArrowRight.setOnClickListener(expandListener)
        }

        fun bind(activity: ActivitiesDataItem) {
            currentActivity = activity

            binding.tvTitleCustomFoodCard.text = "${activity.name} ${activity.duration} ${activity.durationUnit}"
            binding.tvFoodCalories.text = "${activity.caloriesBurned} kkal"
            binding.tvTitleCustomFoodExpand.text = "${activity.name} ${activity.duration} ${activity.durationUnit}"

            binding.icPlusAddFood.setImageResource(R.drawable.ic_calories)
            if (activity.isSelected) {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_checked)
            } else {
                binding.icPlusAddFood.setImageResource(R.drawable.ic_plus_add_food)
            }

            if (activity.isExpanded) {
                binding.tvTitleCustomFoodExpand.visibility = View.VISIBLE
                binding.tvTitleCustomFoodCard.visibility = View.GONE
                binding.expandedView.visibility = View.VISIBLE
                binding.tvFoodCalories.visibility = View.GONE
                binding.icPlusAddFood.visibility = View.GONE

                binding.icArrowRight.setImageResource(R.drawable.arrow_down_custom_food)
                binding.icArrowRight.setSize(36) // Ukuran normal saat expand

                // Isi data untuk view di dalam expanded_view
                binding.tvFoodCaloriesOnExpand.text = "${activity.caloriesBurned.toInt()} kkal"
                binding.tvDescFood.text = activity.description
                Glide.with(itemView.context)
                    .load(activity.photoUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.imgFood)

            } else {
                binding.tvTitleCustomFoodExpand.visibility = View.GONE
                binding.tvTitleCustomFoodCard.visibility = View.VISIBLE
                binding.expandedView.visibility = View.GONE
                binding.tvFoodCalories.visibility = View.VISIBLE
                binding.icPlusAddFood.visibility = View.VISIBLE

                if (activity.isSelected) {
                    binding.icArrowRight.setImageResource(R.drawable.ic_calories)
                    binding.icArrowRight.setSize(32)
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