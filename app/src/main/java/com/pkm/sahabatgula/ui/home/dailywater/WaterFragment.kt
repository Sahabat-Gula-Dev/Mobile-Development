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
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentWaterBinding
import com.pkm.sahabatgula.ui.home.dailywater.history.WaterChartPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.compareTo

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

        glassImageViews.forEach { glassView ->
            glassView.setOnClickListener {
                viewModel.addOneGlassOfWater()
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



                            binding.piDailyWater.apply {
                                icObject.setImageResource(R.drawable.ic_water_circular)
                                tvRemaining.text = state.remainingWater.toString()
                                circularProgressView.apply {
                                    trackColor = ContextCompat.getColor(requireContext(), R.color.blue_water_background)
                                    setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue_water))
                                }
                                circularProgressBackground.apply {
                                    trackColor = ContextCompat.getColor(requireContext(), R.color.blue_water_background)
                                    setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.blue_water_background))
                                }
                                val progress = if (state.maxWater > 0) (state.currentWater.toDouble() / state.maxWater * 100).toInt() else 0
                                Log.d("WaterCheck", "Glasses: ${state.filledGlasses}, " +
                                        "CurrentWater: ${state.currentWater}, " +
                                        "MaxWater: ${state.maxWater}, " +
                                        "Progress: ${(state.currentWater.toDouble() / state.maxWater * 100)}")
                                circularProgressView.progress = progress
                            }

                            // 2. Update tampilan gelas

                            glassImageViews.forEachIndexed { index, imageView ->
                                if (index < state.filledGlasses) {
                                    imageView.setImageResource(R.drawable.img_glass_filled)
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
}