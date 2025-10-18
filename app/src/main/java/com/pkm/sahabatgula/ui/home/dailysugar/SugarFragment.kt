package com.pkm.sahabatgula.ui.home.dailysugar

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
import com.pkm.sahabatgula.ui.home.dailysugar.history.SugarChartPagerAdapter
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.SugarConsumedLevel
import com.pkm.sahabatgula.core.utils.showNutrientExceededDialog
import com.pkm.sahabatgula.databinding.FragmentSugarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.max


@AndroidEntryPoint
class SugarFragment : Fragment() {

    private var _binding: FragmentSugarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SugarViewModel by viewModels()
    private var hasShownOverLimitDialog = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSugarBinding.inflate(inflater, container, false)
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
        viewPager.adapter = SugarChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sugarState.collect { state ->
                when (state) {
                    is SugarState.Success -> {
                        val remaining = max(0, (state.remainingSugar).toInt())
                        binding.piDailySugar.tvRemaining.text = remaining.toString()
                        val progressSugar = ((state.currentSugar / state.maxSugar) * 100).coerceIn(0.0, 100.0)

                        if (progressSugar >= 100 && !hasShownOverLimitDialog) {
                            showNutrientExceededDialog(
                                context = requireContext(),
                                title = "Batas Gula Terlampaui",
                                consumed = state.currentSugar.toInt(),
                                max = state.maxSugar.toInt(),
                                suggestion = "Kamu gampang tergoda dengan makanan manis ya, konsumsi gulamu sudah melebihi batas. "
                            )

                            hasShownOverLimitDialog = true
                        } else if (progressSugar < 100) {
                            hasShownOverLimitDialog = false
                        }

                        val indicatorColor = if ( state.currentSugar > state.maxSugar) {
                            "#B3261E".toColorInt() // merah
                        } else {
                            "#FF3776".toColorInt() // hijau
                        }

                        binding.piDailySugar.tvRemaining.setTextColor(indicatorColor)
                        binding.piDailySugar.circularProgressView.setIndicatorColor(indicatorColor)

                        binding.piDailySugar.circularProgressView.progress = progressSugar.toInt()

                        binding.cardSugarConsumptionLevel.apply {
                            icSugarConsumedLevel.setImageDrawable(ContextCompat.getDrawable(requireContext(), getSugarConsumedLevel(progressSugar.toInt()).icon))
                            tvTitleSugarConsumedLevel.text = getSugarConsumedLevel(progressSugar.toInt()).title
                            tvSubtitleSugarConsumedLevel.text = getSugarConsumedLevel(progressSugar.toInt()).subtitle
                        }

                        binding.cardSugarAction.apply {
                            icAction.setImageResource(R.drawable.ic_water_rectangle_filled)
                            tvTitleAction.text = "Jangan Lupa Minum Air! "
                            tvSubtitleAction.text = "Air membantu proses metabolisme gula dalam tubuh"

                            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_water_background))
                            root.setOnClickListener {
                                findNavController().navigate(R.id.action_log_sugar_to_log_water)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun getSugarConsumedLevel(progress: Int): SugarConsumedLevel {
        return when(progress) {
            in 0..25 -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_very_satisfied,
                title = "Konsumsi Gula Rendah",
                subtitle = "Jangan sampai terlalu semangat dan tergoda dengan yang manis-manis",
                )
            in 26..55 -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_satisfied,
                title = "Konsumsi Gula Sedang",
                subtitle = "Wah, sudah hampir mencapai batas konsumsi gula harian. ",
            )
            in 56..80 -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_moderate,
                title = "Konsumsi Gula Tinggi",
                subtitle = "Kamu pura-pura tidak tahu ya, konsumsi gulamu sangat tinggi hari ini",
            )
            else -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_exceeded,
                title = "Konsumsi Gula Sangat Tinggi",
                subtitle = "Jaga konsumsimu, manis hari ini bisa menjadi pahit dihari esok.",
            )

        }
    }
}