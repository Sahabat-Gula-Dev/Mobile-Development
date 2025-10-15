package com.pkm.sahabatgula.ui.home.dailyfood.detailfood

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.databinding.FragmentDetailFoodBinding
import com.pkm.sahabatgula.ui.state.DialogFoodUiState
import com.pkm.sahabatgula.ui.state.LogFoodStateDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFoodFragment : Fragment() {

    private var _binding: FragmentDetailFoodBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DetailFoodFragmentArgs>()

    private var maxSugar: Double? = null
    private var maxCalories: Int? = null

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

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.loadProfile()
        lifecycleScope.launchWhenStarted {
            viewModel.profile.collect { profile ->
                if (profile != null) {
                    maxSugar = profile.max_sugar
                    maxCalories = profile.max_calories
                    Log.d("DEBUG_NAV", "DetailFoodFragment: maxSugar: $maxSugar, maxCalories: $maxCalories")
                }
            }
        }

        Log.d("DEBUG_NAV", "DetailFoodFragment: onViewCreated dipanggil")

        val foodsItem = args.foodItem
        val foodItemManual = args.foodItemManual

        val foodName = foodsItem?.name ?: foodItemManual?.name
        val foodServingSize = foodsItem?.servingSize ?: foodItemManual?.servingSize
        val servingUnit = foodsItem?.servingUnit ?: foodItemManual?.servingUnit
        val foodServingUnit = servingUnit?.replaceFirstChar { it.uppercase() }
        val foodWeightSize = foodsItem?.weightSize ?: foodItemManual?.weightSize
        val foodWeightUnit = foodsItem?.weightUnit ?: foodItemManual?.weightUnit
        val foodId = foodsItem?.id ?: foodItemManual?.id
        val foodDesc = foodsItem?.description ?: foodItemManual?.description
        val foodCalories = foodsItem?.calories ?: foodItemManual?.calories
        val foodPhoto = foodsItem?.photoUrl ?: foodItemManual?.photoUrl

        if (foodsItem != null || foodItemManual != null) {
            viewModel.fetchFoodDetail(foodId)
        }

        binding.apply {

            tvTitleFood.text =
                "$foodName $foodServingSize $foodServingUnit $foodWeightSize $foodWeightUnit"
            tvFoodDesc.text = foodDesc ?: "-"
            tvCalories.text = "${foodCalories?.toInt() ?: 0} kkal"

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
                showLogFoodConfirmationDialog(
                    foodName = foodName,
                    calories = foodCalories?.toInt(),
                ) {
                    viewModel.logThisFood(foodId, portion)
                }
            } else {
                Toast.makeText(context, "Food ID tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.logFoodStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    val food = viewModel.foodDetail.value?.data
                    val sugarConsumed = food?.sugar ?: 0.0
                    val caloriesConsumed = food?.calories?.toInt() ?: 0

                    val sugarPercent = if (maxSugar != null && maxSugar != 0.0) {
                        (sugarConsumed / maxSugar!!) * 100
                    } else 0.0

                    val caloriesPercent = if (maxCalories != null && maxCalories != 0) {
                        (caloriesConsumed.toDouble() / maxCalories!!) * 100
                    } else 0.0

                    val sugarPercentRounded = String.format("%.0f", sugarPercent)
                    val caloriesPercentRounded = String.format("%.0f", caloriesPercent)

                    val personalizedMessage = "Saat ini kamu telah mengkonsumsi gula sebanyak $sugarPercentRounded% " + "dan kalori sebanyak $caloriesPercentRounded% dari batas konsumsi harianmu."

                    showLogFoodStateDialog(
                        DialogFoodUiState.Success(
                            title = "Yey! Sudah Tersimpan",
                            message = personalizedMessage,
                            imageRes = R.drawable.glubby_food,
                            calorieValue = food?.calories?.toInt(),
                            carbo = food?.carbs?.toInt(),
                            protein = food?.protein?.toInt(),
                            fat = food?.fat?.toInt(),
                            sugar = food?.sugar ?: 0.0,
                            sodium = food?.sodium ?: 0.0,
                            fiber = food?.fiber ?: 0.0,
                            kalium = food?.potassium ?: 0.0
                        )
                    )
                }

                is Resource.Error -> {
                    showLogFoodStateDialog(
                        DialogFoodUiState.Error(
                            title = "Oops, Ada Masalah",
                            message = status.message ?: "Terjadi kesalahan, coba lagi.",
                            imageRes = R.drawable.glubby_error
                        )
                    )
                }

                is Resource.Loading -> {
                    // Tidak menampilkan apapun saat loading
                }
            }
        }
    }

    private fun showLogFoodConfirmationDialog(
        foodName: String?,
        calories: Int?,
        onConfirm: () -> Unit
    ) {
        val context = requireContext()
        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.glubby_calculate)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val size = context.resources.getDimensionPixelSize(R.dimen.dialog_image_size)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
                bottomMargin = 16
                topMargin = 24
            }
        }

        val titleView = TextView(context).apply {
            text = "Catat Makanan Ini?"
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
            setPadding(16, 0, 16, 8)
        }

        val messageView = TextView(context).apply {
            text = "Kamu akan mencatat $foodName dengan total kalori $calories kkal"
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
            setPadding(16, 8, 16, 0)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
            setPadding(16, 16, 16, 16)
            addView(imageView)
            addView(titleView)
            addView(messageView)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(container)
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .setPositiveButton("Catat") { d, _ ->
                onConfirm()
                d.dismiss()
            }
            .create()

        dialog.show()

        val onPrimary = ContextCompat.getColor(context, R.color.md_theme_onPrimary)
        val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        val customFont = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)

        (positiveButton.parent as? View)?.setBackgroundColor(onPrimary)
        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        negativeButton.setBackgroundColor(Color.TRANSPARENT)

        positiveButton.setTextColor(Color.BLACK)
        negativeButton.setTextColor(Color.BLACK)
        positiveButton.typeface = customFont
        negativeButton.typeface = customFont
    }

    private fun showLogFoodStateDialog(state: DialogFoodUiState) {
        val dialog = LogFoodStateDialogFragment.newInstance(state)
        dialog.dismissListener = {
            findNavController().navigate(R.id.action_detail_food_fragment_to_home_graph)
        }
        dialog.show(parentFragmentManager, "LogFoodStateDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}