package com.pkm.sahabatgula.ui.home.dailyfood

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.showNutrientExceededDialog
import com.pkm.sahabatgula.databinding.FragmentFoodBinding
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.FoodChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private var hasShownOverLimitDialog = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabLayoutHistory = binding.tabLayoutHistory
        val viewPager = binding.viewPager
        viewPager.adapter = FoodChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.foodState.collect { state ->
                when (state) {
                    is FoodState.Success -> {
                        binding.piLogFood .apply {
                            val tvRemainingCalories = (state.remainingCalories).coerceIn(0.0,100.0)
                            tvRemaining.text = tvRemainingCalories.toInt().toString()
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_card))
                            val progressCalories = (state.totalCalories/ (state.maxCalories?.toDouble() ?: 0.0))*100
                            tvFormat.text = "kkal tersisa"
                            icObject.setImageResource(R.drawable.ic_food_circular)
                            circularProgressView.apply {
                                progress = progressCalories.toInt()
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.green_card))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.green_card_action)
                            }
                            circularProgressBackground.apply {
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.green_card_action))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.green_card_action)
                            }

                            if (progressCalories >= 100 && !hasShownOverLimitDialog) {
                                state?.maxCalories?.let {
                                    showNutrientExceededDialog(
                                        context = requireContext(),
                                        title = "Batas Kalori Terlampaui",
                                        consumed = state.totalCalories.toInt(),
                                        max = it,
                                        suggestion = "Konsumsi kalorimu sudah melebihi batas harian"
                                    )
                                }

                                hasShownOverLimitDialog = true
                            } else if (progressCalories < 100) {
                                hasShownOverLimitDialog = false
                            }

                            val indicatorColor = state.maxCalories?.let {
                                if ( state.totalCalories > it) {
                                    "#B3261E".toColorInt() // merah
                                } else {
                                    "#088D08".toColorInt() // hijau
                                }
                            }

                            tvRemaining.setTextColor(indicatorColor!!)
                            circularProgressView.setIndicatorColor(indicatorColor)

                        }

                        binding.cardDailyFoodTips.apply {
                            icInfo.setImageResource(R.drawable.ic_question)
                            tvTitleInfo.text = "Tahukah Kamu?"
                            tvSubtitleInfo.text = "Konsumsi protein setiap makan bantu jaga rasa kenyang lebih lama"
                        }

                        binding.cardDailyFoodEntry.apply {
                            icAction.setImageResource(R.drawable.ic_food_filled)
                            tvTitleAction.text = "Catat Konsumsi Harian"
                            tvSubtitleAction.text = "Tetap pantau asupan makananmu dengan cermat ya"
                            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_card_action))
                        }

                        binding.cardHistoryFood.apply {
                            icAction.setImageResource(R.drawable.ic_history)
                            tvTitleAction.text = "Udah Makan Apa Aja Hari Ini?"
                            tvSubtitleAction.text = "Cek ulang makananmu dan pastikan tetap di jalur sehat"
                            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory_background))
                        }

                    }
                    else -> {}
                }
            }
        }
    }
}