package com.pkm.sahabatgula.ui.home.dailywater

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentWaterBinding
import com.pkm.sahabatgula.ui.home.dailywater.history.WaterChartPagerAdapter
import com.pkm.sahabatgula.ui.home.dailywater.history.monthly.MonthlyWaterFragment
import com.pkm.sahabatgula.ui.home.dailywater.history.weekly.WeeklyWaterFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WaterFragment : Fragment() {

    private var _binding: FragmentWaterBinding? = null
    private val binding: FragmentWaterBinding get() = _binding!!
    private val viewModel: WaterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabLayoutHistory = binding.tabLayoutHistory
        val viewPager = binding.viewPager
        val adapter = WaterChartPagerAdapter(this)

        viewPager.adapter = adapter
        TabLayoutMediator(tabLayoutHistory, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Mingguan"
                1 -> tab.text = "Bulanan"
            }
        }.attach()

        val glassImageViews = listOf(
            binding.imgGlass1, binding.imgGlass2, binding.imgGlass3,
            binding.imgGlass4, binding.imgGlass5, binding.imgGlass6,
            binding.imgGlass7, binding.imgGlass8
        )

        glassImageViews.forEachIndexed { index, glassView ->
            glassView.setOnClickListener {
                val currentState = viewModel.waterState.value
                if (currentState is WaterState.Success) {
                    if (index < currentState.filledGlasses && index < 7) {
                        return@setOnClickListener
                    }

                    if (currentState.filledGlasses >= 8) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setIcon(R.drawable.glubby_success)
                            .setTitle("Wah, Kamu Luar Biasa!")
                            .setMessage("Kamu sudah mencapai target minum hari ini! Minum satu gelas lagi akan buat tubuhmu makin segar. Mau lanjut minum?")
                            .setPositiveButton("Ya, Tambah") { dialog, _ ->
                                viewModel.addOneGlassOfWater()
                                triggerChartRefresh()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setIcon(R.drawable.glubby_water)
                            .setTitle("Yuk Tambah 1 Gelas Lagi ")
                            .setMessage("Satu gelas mewakili 250 ml. Gluby siap catat tambahan airmu, mau lanjut?")
                            .setPositiveButton("Tambah") { dialog, _ ->
                                viewModel.addOneGlassOfWater()
                                triggerChartRefresh()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.waterState.collect { state ->
                    when (state) {
                        is WaterState.Loading -> {
                            // Tampilkan UI loading jika perlu
                        }
                        is WaterState.Success -> {

                            binding.cardDidYouKnow.apply {
                                icInfo.setImageResource(R.drawable.ic_question)
                                tvTitleInfo.text = "Tahukah Kamu?"
                                tvSubtitleInfo.text = "Satu ikon gelas mewakili 250 ml air. Cukup minum 8 gelas untuk capai target harianmu"
                            }

                            binding.piDailyWater.apply {
                                tvFormat.text = "ml tersisa"
                                icObject.setImageResource(R.drawable.ic_water_circular)
                                val remainingWater = (state.remainingWater).coerceAtLeast(0)
                                tvRemaining.text = remainingWater.toString()
                                tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_water))
                                circularProgressView.apply {
                                    trackColor = ContextCompat.getColor(requireContext(), R.color.blue_water_background)
                                    setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue_water))
                                }
                                circularProgressBackground.apply {
                                    trackColor = ContextCompat.getColor(requireContext(), R.color.blue_water_background)
                                    setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue_water_background))
                                }
                                val progress = ((state.currentWater.toDouble() / state.maxWater) * 100).toInt()
                                circularProgressView.progress = progress
                            }

                            glassImageViews.forEachIndexed { index, imageView ->
                                val totalGlasses = glassImageViews.size // 8
                                val maxDisplayFilled = totalGlasses - 1 // 7 penuh, 1 terakhir kosong

                                val isLastGlass = index == totalGlasses - 1
                                val shouldFill = index < state.filledGlasses && index < maxDisplayFilled

                                if (shouldFill) {
                                    imageView.setImageResource(R.drawable.img_glass_filled)
                                } else if (isLastGlass) {
                                    imageView.setImageResource(R.drawable.img_glass_empty) // last glass always empty
                                } else {
                                    imageView.setImageResource(R.drawable.img_glass_empty)
                                }
                            }

                        }
                        is WaterState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorEvent.collect { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun triggerChartRefresh() {
        val currentFragments = childFragmentManager.fragments

        currentFragments.forEach { fragment ->
            when (fragment) {
                is WeeklyWaterFragment -> fragment.refreshChartData()
                is MonthlyWaterFragment -> fragment.refreshChartData()
            }
        }
    }

}