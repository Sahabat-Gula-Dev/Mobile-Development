package com.pkm.sahabatgula.ui.home.dailyfat

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
import com.pkm.sahabatgula.databinding.FragmentFatBinding
import com.pkm.sahabatgula.ui.home.dailyfat.history.FatChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.max

@AndroidEntryPoint
class FatFragment : Fragment() {

    private var _binding: FragmentFatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FatViewModel by viewModels()
    private var hasShownOverLimitDialog = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFatBinding.inflate(inflater, container, false)
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
        viewPager.adapter = FatChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fatState.collect { state ->
                when (state) {
                    is FatState.Success -> {
                        binding.piFat .apply {
                            val remaining = max(0, (state.remainingFat).toInt())
                            tvRemaining.text = remaining.toString()
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_fat))
                            tvFormat.text = "gram tersisa"
                            icObject.setImageResource(R.drawable.ic_protein_unfilled)
                            val progressFat = (state.totalFat/ (state.maxFat))*100
                            circularProgressView.apply {
                                progress = progressFat.toInt()
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_fat))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_fat_background)
                            }
                            circularProgressBackground.apply {
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_fat_background))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_fat_background)
                            }

                            if (progressFat >= 100 && !hasShownOverLimitDialog) {
                                showNutrientExceededDialog(
                                    context = requireContext(),
                                    title = "Batas Lemak Terlampaui",
                                    consumed = state.totalFat.toInt(),
                                    max = state.maxFat.toInt(),
                                    suggestion = "Coba pilih makanan yang lebih sehat dan mengandung banyak serat seperti sayuran. Konsumsi lemakmu melampau batas hari ini."
                                )
                                hasShownOverLimitDialog = true
                            } else if (progressFat < 100) {
                                hasShownOverLimitDialog = false
                            }

                            val indicatorColor = if ( state.totalFat > state.maxFat) {
                                "#B3261E".toColorInt() // merah
                            } else {
                                "#FF5023".toColorInt() // hijau
                            }

                            tvRemaining.setTextColor(indicatorColor)
                            circularProgressView.setIndicatorColor(indicatorColor)

                        }

                        binding.cardDailyFatTips.apply {
                            icInfo.setImageResource(R.drawable.ic_information)
                            tvTitleInfo.text = "Tips Buat Kamu?"
                            tvSubtitleInfo.text = "Hindari konsumsi makanan kemasan atau gorengan berulang kali"
                        }

                        binding.cardDidYouKnow.apply {
                            icInfo.setImageResource(R.drawable.ic_question)
                            tvTitleInfo.text = "Tahukah Kamu?"
                            tvSubtitleInfo.text = "Lemak mengandung 2 kali lipat kalori dibanding karbohidrat dan protein per gram"
                        }

                        binding.cardHistoryFat.apply {
                            icAction.setImageResource(R.drawable.ic_history)
                            tvTitleAction.text = "Udah Makan Apa Aja Hari Ini?"
                            tvSubtitleAction.text = "Cek ulang makananmu dan pastikan tetap dalam jalur sehat"
                            root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.brown_action_background))
                        }

                        binding.cardHistoryFat.root.setOnClickListener {
                            findNavController().navigate(R.id.action_log_fat_to_log_history)
                        }

                    }
                    else -> {}
                }
            }
        }
    }

}