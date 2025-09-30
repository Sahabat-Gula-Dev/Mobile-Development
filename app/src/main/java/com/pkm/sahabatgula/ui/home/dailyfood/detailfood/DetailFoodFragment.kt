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
    private val args: DetailFoodFragmentArgs by navArgs()
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

        Log.d("DEBUG_NAV", "DetailFoodFragment: Berhasil dibuat dan ditampilkan (onViewCreated).")

        val foodItem = args.foodItem

        val foodName = foodItem.name
        val foodServingSize = foodItem.servingSize
        val foodServingUnit = foodItem.servingUnit
        val foodWeightSize = foodItem.weightSize
        val foodWeightUnit = foodItem.weightUnit
        val foodId = foodItem.id

        viewModel.fetchFoodDetail(foodId)

        binding.apply {
            tvTitleFood.text = "$foodName $foodServingSize $foodServingUnit $foodWeightSize $foodWeightUnit"
            tvFoodDesc.text = foodItem.description
            tvCalories.text = "${foodItem.calories} kkal"

            Glide.with(requireContext())
                .load(foodItem.photoUrl)
                .placeholder(R.drawable.image_placeholder)
                .into(imgFood)
        }

        //data makro dan mikro nutrient dari api
        viewModel.foodDetail.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val food = resource.data
                    val fat = food?.fat
                    val carbs = food?.carbs
                    val protein = food?.protein
                    val calories = food?.calories?.toDouble()

                    val fatPercent = calories?.let { (fat?.times(9)?.div(it))?.times(100.0) }
                    val carbsPercent = calories?.let { (carbs?.times(4)?.div(it))?.times(100.0) }
                    val proteinPercent = calories?.let { (protein?.times(4)?.div(it))?.times(100.0) }

                    Log.d("DEBUG_FAT", "onViewCreated: $fatPercent")
                    Log.d("DEBUG_CARBS", "onViewCreated: $carbsPercent")
                    Log.d("DEBUG_PROTEIN", "onViewCreated: $proteinPercent")

                    binding.cardFoodNutritions.apply {
                        tvNumberCarbo.text = "${carbs} gr"
                        tvNumberFat.text = "${fat} gr"
                        tvNumberProtein.text = "${protein} gr"
                        tvNumberOfFiber.text = food?.fiber.toString()
                        tvNumberOfPotasium.text = food?.sodium.toString()
                        tvNumberOfSugar.text = food?.sugar.toString()
                        tvNumberOfSalt.text = food?.sodium.toString()

                    }

                    binding.cardFoodNutritions.apply {
                        fatPercent?.let { cpFatIndicator.progress = it.toInt() }
                        Log.d("DEBUG_FAT_PROGRESS", "onViewCreated: ${cpFatIndicator.progress}")
                        carbsPercent?.let { cpCarboIndicator.progress = it.toInt() }
                        Log.d("DEBUG_CARBS_PROGRESS", "onViewCreated: ${cpCarboIndicator.progress}")
                        proteinPercent?.let { cpProteinIndicator.progress = it.toInt() }
                        Log.d("DEBUG_PROTEIN_PROGRESS", "onViewCreated: ${cpProteinIndicator.progress}")
                    }
                }
                else -> {}
            }
        }

        binding.cardInformation.apply{
            icInfo.setImageResource(R.drawable.ic_question)
            tvTitleInfo.text = "Tahukah kamu?"
            tvSubtitleInfo.text = "Gula & garam tersembunyi sering ada di saus, sambal, dan bumbu"

        }

        binding.btnLogThisFood.setOnClickListener {
            val portion = foodItem.servingSize

            if(foodId!=null) {
                viewModel.logThisFood(foodId, portion?:0)
            } else {
                Toast.makeText(context, "Food ID tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.logFoodStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Toast.makeText(context, "Makanan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

}