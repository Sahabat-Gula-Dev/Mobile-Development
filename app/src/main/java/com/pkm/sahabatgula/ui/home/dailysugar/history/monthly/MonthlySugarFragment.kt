package com.pkm.sahabatgula.ui.home.dailysugar.history.monthly

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.databinding.FragmentMonthlyHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class MonthlySugarFragment : Fragment() {

    private var _binding: FragmentMonthlyHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MonthlySugarViewModel by viewModels()

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
                    is MonthlySugarState.Loading -> {
                        // Opsional: Tampilkan loading indicator
                    }
                    is MonthlySugarState.Success -> {
                        setupBarChart(binding.monthlyChart, state.barData, state.xAxisLabels)
                    }
                    is MonthlySugarState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupBarChart(chart: BarChart, data: BarData, xAxisLabels: List<String>) {
        chart.data = data

        // Nonaktifkan interaksi zoom
        chart.setTouchEnabled(true)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)

        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setExtraOffsets(0f, 0f, 0f, 8f)
        // Mengatur font
        val jakartaSans: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.jakarta_sans_family)

        // Sumbu X (Horizontal)
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

        // Sumbu Y Kiri (Vertikal)
        val yAxisLeft = chart.axisLeft
        yAxisLeft.typeface = jakartaSans
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawAxisLine(false)

        // Sumbu Y Kanan (dinonaktifkan)
        chart.axisRight.isEnabled = false

        // Memuat ulang tampilan grafik
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}