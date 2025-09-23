package com.pkm.sahabatgula.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pkm.sahabatgula.R
import android.icu.text.DecimalFormat
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pkm.sahabatgula.core.utils.RiskCategory
import com.pkm.sahabatgula.data.local.room.DailySummaryEntity
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.DailySummaryResponse
import com.pkm.sahabatgula.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()
    val df = DecimalFormat("#.##")

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
            val profile = viewModel.homeRepository.getProfile()
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.summary.collect { summary ->
                    updateSuccessfullUi(profile, summary)
                }
            }
        }
    }

    private fun observeUiEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { effect ->
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

//    private fun handleState(state: HomeState) {
//        binding.root.apply {
//            alpha = if (state is HomeState.Loading) 0.5f else 1.0f
//            isEnabled = state !is HomeState.Loading
//        }
//
//        when (state) {
//            is HomeState.Loading-> {
//
//            }
//            is HomeState.Success -> {
//                updateSuccessfullUi(state.profile, state.summary)
//            }
//            is HomeState.Error -> {
//                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
//
//            }
//        }
//    }

    private fun updateSuccessfullUi(
        profile: ProfileEntity,
        summary: DailySummaryEntity?
    ) {
//        val summaryData = summary.data
//        val nutrients = summary.data?.summary?.nutrients
        binding.userName.text = profile.username?: "Pengguna"


        val caloriesConsumed = summary?.calories?:0
        val carbsConsumed = summary?.carbs?:0
        val proteinConsumed = summary?.protein?:0
        val fatConsumed = summary?.fat?:0
        val sugarConsumed = summary?.sugar?:0
        val sodiumConsumed = summary?.sodium?:0
        val fiberConsumed = summary?.fiber?:0
        val potassiumConsumed = summary?.potassium?:0
        val burned = summary?.burned?:0
        val steps = summary?.steps?:0
        val water = summary?.water?:0
        val maxCalories = profile.max_calories?:0
        val maxCarbs = profile.max_carbs?:0
        val maxProtein = profile.max_protein?:0
        val maxFat = profile.max_fat?:0
        val maxSugar = profile.max_sugar?:0
        val maxSodium = profile.max_natrium?:0
        val maxFiber = profile.max_fiber?:0
        val maxPotassium = profile.max_potassium?:0



        // risk index
        val riskIndex = profile.risk_index
        // risk category
        val riskCategory = getRiskCategory(riskIndex)

        binding.compRiskIndex.bgNumberOfRisk.setCardBackgroundColor(riskCategory.colorRes)
        binding.compRiskIndex.tvNumberOfRisk.text = riskIndex.toString()
        binding.compRiskIndex.tvTitleIndexRisk.text = riskCategory.title
        binding.compRiskIndex.subtitleTvIndexRisk.text = riskCategory.subtitle

        binding.sugarConsumption.apply {
            icProgress.setImageResource(R.drawable.ic_sugar_candy)
            tvNumberOfConsumption.text = sugarConsumed.toString()
            tvTitleProgress.text = "Konsumsi Gula Hari Ini"
            tvNumberOfTotalNutrition.text = " dari $maxSugar gr"
//            tvNumberOfPercentage.text = "${sugarConsumed?.toPercentage(maxSugar?.toInt())}"
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRiskCategory(riskIndex: Int?): RiskCategory {
        return when(riskIndex) {
            in 0..5 -> RiskCategory(
                title = "Risiko Diabetes Sangat Rendah",
                subtitle = "Pertahankan gaya hidup aktif dan pola makan seimbang",
                colorRes = ContextCompat.getColor(requireContext(), R.color.green_dark_low)
            )
            in 6..10 -> RiskCategory(
                title = "Risiko Diabetes Rendah",
                subtitle = "Kondisi cukup baik, jaga pola makan dan aktivitas harian",
                colorRes = ContextCompat.getColor(requireContext(), R.color.green_dark_low)
            )
            in 11..15 -> RiskCategory(
                title = "Risiko Diabetes Sedang",
                subtitle = "Waktunya lebih aktif dan evaluasi kebiasaan makan",
                colorRes = ContextCompat.getColor(requireContext(), R.color.yellow_moderate)
            )
            in 16..20 -> RiskCategory(
                title = "Risiko Diabetes Tinggi",
                subtitle = "Gaya hidup dan riwayat kesehatan menunjukkan risiko tinggi",
                colorRes = ContextCompat.getColor(requireContext(), R.color.red_high)
            )
            else -> RiskCategory(
                title = "Risiko Diabetes Sangat Tinggi",
                subtitle = "Segera konsultasi dan ubah gaya hidup secara drastis",
                colorRes = ContextCompat.getColor(requireContext(), R.color.red_high)
            )
        }

    }

    @SuppressLint("DefaultLocale")
    fun Double.toPercentage(total: Int?): String {
        if (total?.toDouble() == 0.0) return "0%"
        val result = (this / total?.toDouble()!!) * 100
        return String.format("%.2f%%", result)
    }

}