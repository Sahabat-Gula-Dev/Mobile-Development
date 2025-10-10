package com.pkm.sahabatgula.ui.home.dailyactivity.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentActivityBinding
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.ActivityChartPagerAdapter
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.FoodChartPagerAdapter
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
        tabLayoutHistory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabTextView = tab.view.getChildAt(1) as? android.widget.TextView
                tabTextView?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black) // hitam
                )
                tabTextView?.setTypeface(tabTextView.typeface, android.graphics.Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabTextView = tab.view.getChildAt(1) as? android.widget.TextView
                tabTextView?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant) // abu
                )
                tabTextView?.setTypeface(tabTextView.typeface, android.graphics.Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // No-op
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activityState.collect { state ->
                when (state) {
                    is ActivityState.Success -> {
                        binding.piLogCalories.apply {
                            tvRemaining.text = state.burned.toString()

                            val burned = state.burned ?: 0
                            val maxBurned = state.maxBurned ?: 0

                            val progressCalories = if (maxBurned > 0) {
                                (burned.toDouble() / maxBurned.toDouble()) * 100
                            } else {
                                0.0
                            }
                            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_activity_calory))
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