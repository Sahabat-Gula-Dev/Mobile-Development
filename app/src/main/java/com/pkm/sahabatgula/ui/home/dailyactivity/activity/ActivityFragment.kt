package com.pkm.sahabatgula.ui.home.dailyactivity.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentActivityBinding
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.ActivityChartPagerAdapter
import com.pkm.sahabatgula.ui.home.dailysugar.history.SugarChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActivityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayoutHistory = binding.tabLayoutHistory
        val viewPager = binding.viewPager
        viewPager.adapter = ActivityChartPagerAdapter(this)

        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activityState.collect { state ->
                when (state) {
                    is ActivityState.Success -> {
                        binding.piLogCalories.apply {
                            tvRemaining.text = state.totalCalories.toInt().toString()
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory))
                            val progressCalories = (state.totalCalories/ (state.maxCalories?.toDouble() ?: 0.0))
                            tvFormat.text = "kkal"
                            icObject.setImageResource(R.drawable.ic_activity_dumble_circle)
                            circularProgressView.apply {
                                progress = progressCalories.toInt()
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_activity_calory_background)
                            }
                            circularProgressBackground.apply {
                                setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory_background))
                                trackColor = ContextCompat.getColor(requireContext(), R.color.brown_activity_calory_background)
                            }
                        }

                        binding.cardInformation.apply {
                            icInfo.setImageResource(R.drawable.ic_question)
                            tvTitleInfo.text = "Tahukah Kamu?"
                            tvSubtitleInfo.text = "Aktivitas ringan seperti berjalan kaki pun membantu menjaga kadar gula tetap stabil"
                        }

                    }
                    else -> {}
                }
            }
        }
    }
}