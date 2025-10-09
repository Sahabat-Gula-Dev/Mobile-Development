package com.pkm.sahabatgula.ui.home.dailyfood.charthistory.weekly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentWeeklyHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeeklyFoodFragment : Fragment() {

    private var _binding: FragmentWeeklyHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeeklyFoodViewModel by viewModels()
    private var selectedEntry: BarEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeeklyHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUiState()

        binding.weeklyChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectedEntry = e as? BarEntry
                binding.weeklyChart.invalidate()
            }
            override fun onNothingSelected() {
                selectedEntry = null
                binding.weeklyChart.invalidate()
            }
        })
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WeeklyFoodState.Loading -> {
                        // Tampilkan loading indicator
                    }
                    is WeeklyFoodState.Success -> {
                        // Panggil fungsi setup grafik dengan data dari ViewModel
                        setupBarChart(binding.weeklyChart, state.barData, state.xAxisLabels)
                    }
                    is WeeklyFoodState.Error -> {
                    }
                }
            }
        }
    }

    private fun setupBarChart(chart: BarChart, data: BarData, xAxisLabels: List<String>) {

        // Nonaktifkan interaksi
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false) // <-- Menonaktifkan zoom
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
        chart.setExtraOffsets(10f, 0f, 0f, 8f)

        // Konfigurasi umum
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        // font
        val font = resources.getFont(R.font.jakarta_sans_family)

        // Sumbu X (Horizontal)
        val xAxis = chart.xAxis
        xAxis.typeface = font
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return xAxisLabels.getOrNull(value.toInt()) ?: ""
            }
        }

        // Sumbu Y Kiri (Vertikal)
        val yAxisLeft = chart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.typeface = font
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(false)


        // Sumbu Y Kanan
        chart.axisRight.isEnabled = false

        // Refresh grafik untuk menampilkan data
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}