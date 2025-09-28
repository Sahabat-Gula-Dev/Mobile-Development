package com.pkm.sahabatgula.ui.home.dailycarbo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentCarboBinding
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.ActivityChartPagerAdapter
import com.pkm.sahabatgula.ui.home.dailycarbo.history.CarboChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class CarboFragment : Fragment() {

    private var _binding: FragmentCarboBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CarboViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarboBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayoutHistory = binding.tabLayoutHistory
        val viewPager = binding.viewPager
        viewPager.adapter = CarboChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.carboState.collect { state ->
                when (state) {
                    is CarboState.Success -> {
                        binding.piLogFood.apply {
                            tvRemaining.text = state.totalCarbo.toInt().toString()
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow_carbo_text))
                            tvFormat.text = "gram tersisa"
                            icObject.setImageResource(R.drawable.ic_carbo_rice_filled)
                            val progressCarbo = (state.totalCarbo/ (state.maxCarbo))
                            circularProgressView.apply {
                                progress = progressCarbo.toInt()
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.yellow_carbo))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.yellow_carbo_background)
                            }
                            circularProgressBackground.apply {
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.yellow_carbo_background))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.yellow_carbo_background)
                            }
                        }

                        binding.cardDidYouKnow.apply {
                            icInfo.setImageResource(R.drawable.ic_question)
                            tvTitleInfo.text = "Tahukah Kamu?"
                            tvSubtitleInfo.text = "Tubuh hanya butuh sekitar 45–65% karbohidrat dari total kalori harian"
                        }

                        binding.cardHistoryFood.apply {
                            icAction.setImageResource(R.drawable.ic_history)
                            tvTitleAction.text = "Udah Makan Apa Aja Hari Ini?"
                            tvSubtitleAction.text = "Cek ulang makananmu dan pastikan kamu tetap dalam jalur sehat"
                        }

                        binding.cardHistoryFood.root.setOnClickListener {
                            findNavController().navigate(R.id.action_log_carbo_to_log_food)
                        }

                    }
                    else -> {}
                }
            }
        }
    }

}