package com.pkm.sahabatgula.ui.home.dailycarbo.history.monthly


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

sealed class MonthlyCarboState {
    object Loading : MonthlyCarboState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : MonthlyCarboState()
    data class Error(val message: String) : MonthlyCarboState()
}


@HiltViewModel
class MonthlyCarboViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthlyCarboState>(MonthlyCarboState.Loading)
    val uiState: StateFlow<MonthlyCarboState> = _uiState

    init {
        loadMonthlyCarboData()
    }

    private fun loadMonthlyCarboData() {
        viewModelScope.launch {
            _uiState.value = MonthlyCarboState.Loading
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()
            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlyCarboState.Error("Data bulanan tidak ditemukan.")
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

        val currentMonthColor = "#C89632".toColorInt()
        val previousMonthColor = "#F4EAD6".toColorInt()

        monthSlots.forEachIndexed { index, yearMonth ->
            val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
            val dataForMonth = monthlyData.find { entity ->
                entity.date.let { YearMonth.parse(it, monthFormatter) } == yearMonth
            }
            val carboAmount = dataForMonth?.carbs ?: 0.0
            entries.add(BarEntry(index.toFloat(), carboAmount.toFloat()))
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
        _uiState.value = MonthlyCarboState.Success(barData, xAxisLabels)
    }
}