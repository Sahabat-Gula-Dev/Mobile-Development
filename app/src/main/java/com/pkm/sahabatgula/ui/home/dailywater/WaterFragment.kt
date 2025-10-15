package com.pkm.sahabatgula.ui.home.dailywater

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.showWaterDialog
import com.pkm.sahabatgula.databinding.FragmentWaterBinding
import com.pkm.sahabatgula.ui.home.dailywater.history.WaterChartPagerAdapter
import com.pkm.sahabatgula.ui.home.dailywater.history.monthly.MonthlyWaterFragment
import com.pkm.sahabatgula.ui.home.dailywater.history.weekly.WeeklyWaterFragment
import com.pkm.sahabatgula.ui.state.DialogFoodUiState
import com.pkm.sahabatgula.ui.state.LogFoodStateDialogFragment
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

        binding.cardDidYouKnow.apply {
            icInfo.setImageResource(R.drawable.ic_question)
            tvTitleInfo.text = "Tahukah Kamu?"
            tvSubtitleInfo.text = "Satu ikon gelas mewakili 250 ml air. Cukup minum 8 gelas untuk capai target harianmu"
        }


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
                        showWaterDialog(
                            context = requireContext(),
                            imageRes = R.drawable.glubby_success,
                            title = "Wah, Kamu Luar Biasa!",
                            subtitle = "Kamu sudah mencapai target minum hari ini! Minum satu gelas lagi akan buat tubuhmu makin segar. Mau lanjut minum?",
                            positiveText = "Ya, Tambah"
                        ) {
                            viewModel.addOneGlassOfWater()
                            triggerChartRefresh()
                        }
                    } else {
                        showWaterDialog(
                            context = requireContext(),
                            imageRes = R.drawable.glubby_water,
                            title = "Yuk Tambah 1 Gelas Lagi",
                            subtitle = "Kamu mau minum 250 mL? Glubby bantu catat ya?",
                            positiveText = "Tambah"
                        ) {
                            viewModel.addOneGlassOfWater()
                            triggerChartRefresh()
                        }
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.waterState.collect { state ->
                    when (state) {
                        is WaterState.Loading -> {
                        }
                        is WaterState.Success -> {
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
                                val totalGlasses = glassImageViews.size
                                val maxDisplayFilled = totalGlasses - 1

                                val isLastGlass = index == totalGlasses - 1
                                val shouldFill = index < state.filledGlasses && index < maxDisplayFilled

                                if (shouldFill) {
                                    imageView.setImageResource(R.drawable.img_glass_filled)
                                } else if (isLastGlass) {
                                    imageView.setImageResource(R.drawable.img_glass_empty)
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
                viewModel.logWaterStatus.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                        }
                        is Resource.Success -> {
                            Log.d("WATER FRAGMENT", "WaterFragment: Success")
                            showLogWaterStateDialog(
                                DialogFoodUiState.Success(
                                    title = "Yey! Satu Gelas Lagi",
                                    message = "Ternyata bukan hanya niat, sedikit lagi akan menjadi kebiasaan yang hebat",
                                    imageRes = R.drawable.glubby_water,
                                    calorieValue = null
                                )
                            )
                        }
                        is Resource.Error -> {
                            showLogWaterStateDialog(
                                DialogFoodUiState.Error(
                                    title = "Oops, Ada Masalah",
                                    message = "Terjadi kesalahan saat mencatat air, periksa koneksi internetmu atau coba ulangi lagi",
                                    imageRes = R.drawable.glubby_error
                                )
                            )
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

    private fun showLogWaterStateDialog(state: DialogFoodUiState) {
        val dialog = LogFoodStateDialogFragment.newInstance(state)
        dialog.show(parentFragmentManager, "LogWaterStateDialog")
    }



}