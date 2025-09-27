package com.pkm.sahabatgula.ui.home.dailysugar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.ui.home.dailysugar.history.ChartPagerAdapter
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.SugarConsumedLevel
import com.pkm.sahabatgula.databinding.FragmentSugarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SugarFragment : Fragment() {

    private var _binding: FragmentSugarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SugarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSugarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayoutHistory = binding.tabLayoutHistory
        val viewPager = binding.viewPager
        viewPager.adapter = ChartPagerAdapter(this)

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
                        binding.piDailySugar.tvRemaining.text = state.remainingSugar.toInt().toString()
                        Log.d("SugarFragment", "Remaining Sugar: ${state.remainingSugar}")
                        // berapa max sugar
                        Log.d("SugarFragment", "Max Sugar: ${state.maxSugar}")
                        // berapa sugar saat ini
                        Log.d("SugarFragment", "Current Sugar: ${state.currentSugar}")

                        val progressSugar = (state.currentSugar / state.maxSugar) * 100

                        binding.piDailySugar.piCircularProgress.progress = progressSugar.toInt()

                        binding.cardSugarConsumptionLevel.apply {
                            icSugarConsumedLevel.setImageDrawable(ContextCompat.getDrawable(requireContext(), getSugarConsumedLevel(progressSugar.toInt()).icon))
                            tvTitleSugarConsumedLevel.text = getSugarConsumedLevel(progressSugar.toInt()).title
                            tvSubtitleSugarConsumedLevel.text = getSugarConsumedLevel(progressSugar.toInt()).subtitle
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
                subtitle = "Kamu baru mengonsumsi sedikit gula hari ini. Tetap jaga pola makan seimbang ya",
                )
            in 26..55 -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_satisfied,
                title = "Konsumsi Gula Sedang",
                subtitle = "Konsumsi gulamu sudah lebih dari setengah batas harian",
            )
            in 56..80 -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_moderate,
                title = "Konsumsi Gula Tinggi",
                subtitle = "Kamu sudah hampir mencapai batas konsumsi harian",
            )
            else -> SugarConsumedLevel(
                icon = R.drawable.ic_sentiment_exceeded,
                title = "Konsumsi Gula Sangat Tinggi",
                subtitle = "Konsumsi gula berlebih dapat meningkatkan risiko diabetes",
            )

        }
    }
}