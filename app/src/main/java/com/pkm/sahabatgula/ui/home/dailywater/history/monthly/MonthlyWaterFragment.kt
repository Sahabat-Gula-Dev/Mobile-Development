package com.pkm.sahabatgula.ui.home.dailywater.history.monthly

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.ValueFormatter
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentMonthlyHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MonthlyWaterFragment : Fragment() {

    private var _binding: FragmentMonthlyHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MonthlyWaterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthlyHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MonthlyWaterState.Loading -> {}
                    is MonthlyWaterState.Success -> {
                        setupBarChart(binding.monthlyChart, state.barData, state.xAxisLabels)
                    }
                    is MonthlyWaterState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupBarChart(chart: BarChart, data: BarData, xAxisLabels: List<String>) {
        if (chart.data != null) {
            chart.data = data
            chart.notifyDataSetChanged()
            chart.invalidate()
        } else {
            chart.data = data
            chart.invalidate()
        }

        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setExtraOffsets(10f, 0f, 0f, 8f)

        val jakartaSans: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.jakarta_sans_family)

        val xAxis = chart.xAxis
        xAxis.typeface = jakartaSans
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return xAxisLabels.getOrNull(value.toInt()) ?: ""
            }
        }

        val yAxisLeft = chart.axisLeft
        yAxisLeft.typeface = jakartaSans
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(false)

        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    fun refreshChartData() {
        viewModel.reloadMonthlyData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
