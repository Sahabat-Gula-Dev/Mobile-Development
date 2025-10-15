package com.pkm.sahabatgula.ui.home.dailywater.history.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.core.graphics.toColorInt
import java.time.YearMonth

sealed class MonthlyWaterState {
    object Loading : MonthlyWaterState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : MonthlyWaterState()
    data class Error(val message: String) : MonthlyWaterState()
}

@HiltViewModel
class MonthlyWaterViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthlyWaterState>(MonthlyWaterState.Loading)
    val uiState: StateFlow<MonthlyWaterState> = _uiState

    init {
        loadMonthlyWaterData()
    }

    private fun loadMonthlyWaterData() {
        viewModelScope.launch {
            _uiState.value = MonthlyWaterState.Loading
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()
            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlyWaterState.Error("Data bulanan tidak ditemukan.")
                return@launch
            }
            processDataForChart(monthlyData)
        }
    }

    private fun processDataForChart(monthlyData: List<SummaryEntity>) {
        val currentMonth = YearMonth.now()
        val locale = Locale.forLanguageTag("id-ID")
        val monthSlots = (0..6).map { currentMonth.minusMonths(it.toLong()) }.reversed()
        val xAxisLabels = monthSlots.map {
            it.format(DateTimeFormatter.ofPattern("MMM", locale))
        }

        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>()
        val currentMonthColor = "#2196F3".toColorInt()
        val previousMonthColor = "#D3EAFD".toColorInt()

        monthSlots.forEachIndexed { index, yearMonth ->
            val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
            val dataForMonth = monthlyData.find { entity ->
                entity.date.let { YearMonth.parse(it, monthFormatter) } == yearMonth
            }
            val waterAmount = dataForMonth?.water ?: 0
            entries.add(BarEntry(index.toFloat(), waterAmount.toFloat()))
            if (yearMonth == currentMonth) {
                barColors.add(currentMonthColor)
            } else {
                barColors.add(previousMonthColor)
            }
        }

        val dataSet = BarDataSet(entries, "Konsumsi Gula Bulanan")
        dataSet.setDrawValues(false)
        dataSet.colors = barColors
        dataSet.isHighlightEnabled = false
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        _uiState.value = MonthlyWaterState.Success(barData, xAxisLabels)
    }

    fun reloadMonthlyData() {
        viewModelScope.launch {
            homeRepository.refreshDailySummary()
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()
            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlyWaterState.Error("Data bulanan tidak ditemukan.")
            } else {
                processDataForChart(monthlyData)
            }
        }
    }
}
