//package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood.search
//
//import android.view.ViewGroup
//import androidx.paging.PagingDataAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.pkm.sahabatgula.data.remote.model.FoodItem
//import com.pkm.sahabatgula.databinding.ItemCardCustomFoodBinding
//import com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.FoodPagingAdapter
//
//class CustomFoodPagingAdapter (
//    private val onItemClick: (FoodItem) -> Unit
//): PagingDataAdapter<FoodItem, CustomFoodPagingAdapter.FoodCustomViewHolder>(FOOD_COMPARATOR) {
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): CustomFoodPagingAdapter.FoodCustomViewHolder {
//        TODO("Not yet implemented")
//    }
//
//    class FoodCustomViewHolder(
//        private val binding: ItemCardCustomFoodBinding,
//        private val onSelectClick: (FoodItem) -> Unit,
//        private val onExpandCLick: (FoodItem) -> Unit
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(item: FoodItem) {
//            binding.tvTitleCustomFoodCard.text = "${ item.name } + ${item.servingSize}+ ${item.servingUnit}+ ${item.weightSize}+ ${item.weightUnit}"
//            binding.imgFood = item.photoUrl
//        }
//    }
//
//}