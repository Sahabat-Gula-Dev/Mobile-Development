package com.pkm.sahabatgula.ui.home.dailyfood.detailfood

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.databinding.FragmentDetailFoodBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFoodFragment : Fragment() {

    private var _binding: FragmentDetailFoodBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DetailFoodFragmentArgs>()


    private val viewModel: DetailFoodViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DEBUG_NAV", "DetailFoodFragment: onViewCreated dipanggil")

        val foodsItem = args.foodItem
        val foodItemManual = args.foodItemManual

        // pilih sumber data sesuai asal fragment
        val foodName = foodsItem?.name ?: foodItemManual?.name
        val foodServingSize = foodsItem?.servingSize ?: foodItemManual?.servingSize
        val foodServingUnit = foodsItem?.servingUnit ?: foodItemManual?.servingUnit
        val foodWeightSize = foodsItem?.weightSize ?: foodItemManual?.weightSize
        val foodWeightUnit = foodsItem?.weightUnit ?: foodItemManual?.weightUnit
        val foodId = foodsItem?.id ?: foodItemManual?.id
        val foodDesc = foodsItem?.description ?: foodItemManual?.description
        val foodCalories = foodsItem?.calories ?: foodItemManual?.calories
        val foodPhoto = foodsItem?.photoUrl ?: foodItemManual?.photoUrl

        // fetch detail kalau ada id (scan case)
        if (foodsItem != null || foodItemManual !=null) {
            viewModel.fetchFoodDetail(foodId)
        }

        binding.apply {
            tvTitleFood.text = "$foodName $foodServingSize $foodServingUnit $foodWeightSize $foodWeightUnit"
            tvFoodDesc.text = foodDesc ?: "-"
            tvCalories.text = "${foodCalories ?: 0} kkal"

            Glide.with(requireContext())
                .load(foodPhoto)
                .placeholder(R.drawable.image_placeholder)
                .into(imgFood)
        }

        // kalau data dari API (scan), observe detail
        viewModel.foodDetail.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val food = resource.data
                    // isi nutrisi dari API
                    binding.cardFoodNutritions.apply {
                        tvNumberCarbo.text = "${food?.carbs} gr"
                        tvNumberFat.text = "${food?.fat} gr"
                        tvNumberProtein.text = "${food?.protein} gr"
                        tvNumberOfFiber.text = food?.fiber.toString()
                        tvNumberOfPotasium.text = food?.potassium.toString()
                        tvNumberOfSugar.text = food?.sugar.toString()
                        tvNumberOfSalt.text = food?.sodium.toString()
                    }
                }
                else -> {}
            }
        }

        binding.btnLogThisFood.setOnClickListener {
            val portion = foodServingSize ?: 0
            if (foodId != null) {
                viewModel.logThisFood(foodId, portion)
            } else {
                Toast.makeText(context, "Food ID tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }


}