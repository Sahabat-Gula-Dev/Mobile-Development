package com.pkm.sahabatgula.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pkm.sahabatgula.R
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.pkm.sahabatgula.core.utils.RiskCategory
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.SummaryResponse
import com.pkm.sahabatgula.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()
        observeUiEffect()

    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Hanya collect dari satu sumber: uiState
                viewModel.uiState.collect { state ->
                    handleState(state) // <-- Gunakan fungsi handleState yang sudah benar
                }
            }
        }
    }

    private fun observeUiEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when(effect) {
                        is HomeEffect.ShowToast -> {
                            Toast.makeText(requireContext(), effect.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun handleState(state: HomeState) {
        binding.root.apply {
            alpha = if (state is HomeState.Loading) 0.5f else 1.0f
            isEnabled = state !is HomeState.Loading
        }

        when (state) {
            is HomeState.Loading -> {
                Log.d("HomeFragment", "UI State: Loading")
            }
            is HomeState.Success -> {
                Log.d("HomeFragment", "UI State: Success, updating UI.")

                updateSuccessfullUi(state.profile, state.summary)
            }
            is HomeState.Error -> {
                Log.e("HomeFragment", "UI State: Error: ${state.message}")
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateSuccessfullUi(
        profile: ProfileEntity,
        summary: SummaryResponse
    ) {
//        val summaryData = summary.data
        val nutrients = summary.data.daily.nutrients

        var username = profile.username
        username = username?.replaceFirstChar { it.uppercase() }

        binding.userName.text = username
        val caloriesConsumed = nutrients.calories
        val carbsConsumed = summary.data.daily.nutrients.carbs
        val proteinConsumed = summary.data.daily.nutrients.protein
        val fatConsumed = summary.data.daily.nutrients.fat
        val sugarConsumed = summary.data.daily.nutrients.sugar?:0.0
        val steps = summary.data.daily.steps?:0
        val waterIntake = summary.data.daily.water?:0
        val maxCalories = profile.max_calories?:0
        val maxCarbs = profile.max_carbs?:0.0
        val maxProtein = profile.max_protein?:0.0
        val maxFat = profile.max_fat?:0.0
        val maxSugar = profile.max_sugar?:0.0

        // risk index
        val riskIndex = profile.risk_index
        // risk category
        val riskCategory = getRiskCategory(riskIndex)
        val numberRisk = binding.compRiskIndex.tvNumberOfRisk
        // risk
        binding.compRiskIndex.bgNumberOfRisk.setCardBackgroundColor(riskCategory.colorRes)
        numberRisk.text = String.format("%02d", riskIndex)
        binding.compRiskIndex.tvTitleIndexRisk.text = riskCategory.title
        binding.compRiskIndex.subtitleTvIndexRisk.text = riskCategory.subtitle

        // sugar
        binding.sugarConsumption.apply {
            icProgress.setImageResource(R.drawable.ic_sugar_candy)
            tvNumberOfConsumption.text = String.format("%.1f", sugarConsumed)
            tvTitleProgress.text = "Konsumsi Gula Hari Ini"
            tvNumberOfTotalNutrition.text = " dari ${maxSugar.toInt()} gr"
            tvNumberOfPercentage.text = sugarConsumed.toDouble().toPercentage(maxSugar?.toInt())
        }

        // carbo
        binding.carboConsumption.apply {
            icProgress.setImageResource(R.drawable.ic_carbo_rice)
            tvNumberOfConsumption.text = DoubleToZeroInt(carbsConsumed)
            tvTitleProgress.text = "Karbohidrat"
            tvNumberOfTotalNutrition.text = " dari ${maxCarbs.toInt()} gr"
            icGraphicOfProgress.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow_carbo)
            tvNumberOfPercentage.text = "${carbsConsumed?.toPercentage(maxCarbs.toInt())}"
        }

        // fat
        binding.fatConsumption.apply {
            icProgress.setImageResource(R.drawable.ic_fat)
            tvNumberOfConsumption.text = DoubleToZeroInt(fatConsumed)
            tvTitleProgress.text = "Lemak"
            tvNumberOfTotalNutrition.text = " dari ${maxFat.toInt()} gr"
            icGraphicOfProgress.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_fat)
            tvNumberOfPercentage.text = "${fatConsumed?.toPercentage(maxFat.toInt())}"
        }

        // protein
        binding.proteinConsumption.apply {
            icProgress.setImageResource(R.drawable.ic_protein)
            tvNumberOfConsumption.text = DoubleToZeroInt(proteinConsumed)
            tvTitleProgress.text = "Protein"
            tvNumberOfTotalNutrition.text = " dari ${maxProtein.toInt()} gr"
            icGraphicOfProgress.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.brown_protein)
            tvNumberOfPercentage.text = "${proteinConsumed?.toPercentage(maxProtein.toInt())}"
        }

        // protein
        binding.waterIntakeCard.apply {
            icProgress.setImageResource(R.drawable.ic_water_intake_glass)
            tvNumberOfConsumption.text = waterIntake.toString()
            tvTitleProgress.text = "Asupan Air"
            tvNumberOfTotalNutrition.text = " dari 2000 ML"
            icGraphicOfProgress.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_water)
            tvNumberOfPercentage.text = "${waterIntake.toDouble()?.toPercentage(total = 2500)}"
        }

        // step
        binding.cardTotalSteps.apply {
            icProgress.setImageResource(R.drawable.ic_steps_total)
            tvNumberOfConsumption.text = steps.toString()
            tvTitleProgress.text = "Total Langkah Hari Ini"
            tvNumberOfTotalNutrition.text = " dari 6000 Langkah"
            icGraphicOfProgress.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_steps)
            tvNumberOfPercentage.text = "${steps.toDouble()?.toPercentage(total = 6000)}"
        }

        binding.tvCaloriesConsumed.text = caloriesConsumed?.toInt().toString()
        binding.tvCaloriesNeeded.text = maxCalories.toString()


        // circular progress calories
        val progressCalories = (caloriesConsumed!! / maxCalories.toDouble()) * 100
        binding.circularProgressCalories.piCircularProgress.progress = progressCalories.toInt()
        binding.circularProgressCalories.tvRemaining.text = (maxCalories.toDouble()- caloriesConsumed).toInt().toString()

        binding.noConsumptionToday.root.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_card_action)
        binding.noConsumptionToday.apply {
            icAction.setImageResource(R.drawable.ic_food_filled)
            tvTitleAction.text = "Belum Catat Konsumsi Harian?"
            tvSubtitleAction.text = "Asupan harianmu menentukan kadar gula dalam tubuh"
        }

        binding.haveMovementToday.apply {
            icAction.setImageResource(R.drawable.ic_activity_dumble)
            tvTitleAction.text = "Sudah Bergerak Hari Ini?"
            tvSubtitleAction.text = "Aktivitas fisik bantu bakar kalori dan jaga kadar gula tetap stabil"
            root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory_background))
        }

        binding.circularProgressCalories.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_food)
        }
        binding.sugarConsumption.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_sugar)
        }
        binding.noConsumptionToday.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_food)
        }
        binding.haveMovementToday.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_activity)
        }
        binding.carboConsumption.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_carbo)
        }
        binding.fatConsumption.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_fat)
        }
        binding.proteinConsumption.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_protein)
        }
        binding.waterIntakeCard.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_water)
        }
        binding.cardTotalSteps.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_log_step)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun anyToZeroInt (a: Any): String {
        return if (a == 0.0) "0" else a.toString()
    }

    private fun DoubleToZeroInt (a: Double?): String {
        return if (a == 0.0) a.toInt().toString() else a.toString()
    }

    private fun getRiskCategory(riskIndex: Int?): RiskCategory {
        return when(riskIndex) {
            in 0..3 -> RiskCategory(
                title = "Risiko Sangat Rendah",
                subtitle = "Pertahankan gaya hidup aktif dan pola makan seimbang",
                colorRes = ContextCompat.getColor(requireContext(), R.color.green_dark_low)
            )
            in 4..8 -> RiskCategory(
                title = "Risiko Diabetes Rendah",
                subtitle = "Kondisi cukup baik, jaga pola makan dan aktivitas harian",
                colorRes = ContextCompat.getColor(requireContext(), R.color.green_dark_low)
            )
            in 9..12 -> RiskCategory(
                title = "Risiko Diabetes Sedang",
                subtitle = "Waktunya lebih aktif dan evaluasi kebiasaan makan",
                colorRes = ContextCompat.getColor(requireContext(), R.color.yellow_moderate)
            )
            in 13..20 -> RiskCategory(
                title = "Risiko Diabetes Tinggi",
                subtitle = "Gaya hidup dan riwayat kesehatan menunjukkan risiko tinggi",
                colorRes = ContextCompat.getColor(requireContext(), R.color.red_high)
            )
            else -> RiskCategory(
                title = "Risiko Sangat Tinggi",
                subtitle = "Segera konsultasi dan ubah gaya hidup secara drastis",
                colorRes = ContextCompat.getColor(requireContext(), R.color.red_high)
            )
        }

    }

    @SuppressLint("DefaultLocale")
    infix fun Double.toPercentage(total: Int?): String {
        if (total?.toDouble() == 0.0) return "0%"
        val result = (this / total?.toDouble()!!) * 100
        return String.format("%d%%", result.roundToInt())
    }

}