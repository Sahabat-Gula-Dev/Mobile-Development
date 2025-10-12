package com.pkm.sahabatgula.ui.home.dailyprotein

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.showNutrientExceededDialog
import com.pkm.sahabatgula.databinding.FragmentProteinBinding
import com.pkm.sahabatgula.ui.home.dailycarbo.CarboState
import com.pkm.sahabatgula.ui.home.dailyprotein.history.ProteinChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.max

@AndroidEntryPoint
class ProteinFragment : Fragment() {

    private var _binding: FragmentProteinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProteinViewModel by viewModels()
    private var hasShownOverLimitDialog = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProteinBinding.inflate(inflater, container, false)
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
        viewPager.adapter = ProteinChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.proteinState.collect { state ->
                when (state) {
                    is ProteinState.Success -> {
                        binding.piProtein .apply {
                            val remaining = max(0, (state.remainingProtein).toInt())
                            tvRemaining.text = remaining.toString()
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_protein_text))
                            tvFormat.text = "gram tersisa"
                            icObject.setImageResource(R.drawable.ic_protein_unfilled)
                            val progressProtein = (state.totalProtein/ (state.maxProtein))*100
                            circularProgressView.apply {
                                progress = progressProtein.toInt()
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_protein))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_protein_background)
                            }
                            circularProgressBackground.apply {
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_protein_background))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_protein_background)
                            }

                            if (progressProtein >= 100 && !hasShownOverLimitDialog) {
                                showNutrientExceededDialog(
                                    requireContext(),
                                    "Batas Protein Terlampaui",
                                    state.totalProtein.toInt(),
                                    state.maxProtein.toInt(),
                                    "Asupan proteinmu sudah berlebihan. Yuk, kendalikan porsimu agar tetap seimbang"
                                )
                                hasShownOverLimitDialog = true
                            } else if (progressProtein < 100) {
                                hasShownOverLimitDialog = false
                            }

                            val indicatorColor = if ( state.totalProtein > state.maxProtein) {
                                "#B3261E".toColorInt() // merah
                            } else {
                                "#B35408".toColorInt() // hijau
                            }

                            tvRemaining.setTextColor(indicatorColor)
                            circularProgressView.setIndicatorColor(indicatorColor)

                        }

                        binding.cardDailyProteinTips.apply {
                            icInfo.setImageResource(R.drawable.ic_information)
                            tvTitleInfo.text = "Tips Buat Kamu?"
                            tvSubtitleInfo.text = "Padukan sumber hewani dan nabati untuk asupan protein yang lengkap"
                        }

                        binding.cardDidYouKnow.apply {
                            icInfo.setImageResource(R.drawable.ic_question)
                            tvTitleInfo.text = "Tahukah Kamu?"
                            tvSubtitleInfo.text = "Protein adalah komponen penting pembentuk otot, hormon, dan sistem imun"
                        }

                        binding.cardHistoryFood.apply {
                            icAction.setImageResource(R.drawable.ic_history)
                            tvTitleAction.text = "Udah Makan Apa Aja Hari Ini?"
                            tvSubtitleAction.text = "Cek ulang makananmu dan pastikan tetap dalam jalur sehat"
                        }
                        binding.cardHistoryFood.root.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.brown_action_background))

                        binding.cardHistoryFood.root.setOnClickListener {
                            findNavController().navigate(R.id.action_log_protein_to_log_history)
                        }

                    }
                    else -> {}
                }
            }
        }
    }

}