package com.pkm.sahabatgula.ui.home.dailyfat.history.weekly


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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import androidx.core.graphics.toColorInt

sealed class WeeklyFatState {
    object Loading : WeeklyFatState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : WeeklyFatState()
    data class Error(val message: String) : WeeklyFatState()
}

@HiltViewModel
class WeeklyFatViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeeklyFatState>(WeeklyFatState.Loading)
    val uiState: StateFlow<WeeklyFatState> = _uiState

    init {
        loadWeeklyFatData()
    }

    private fun loadWeeklyFatData() {
        viewModelScope.launch {
            _uiState.value = WeeklyFatState.Loading
            val weeklyData = homeRepository.observeWeeklySummary().firstOrNull()

            if (weeklyData.isNullOrEmpty()) {
                _uiState.value = WeeklyFatState.Error("Data mingguan tidak ditemukan.")
                return@launch
            }

            // Proses data untuk grafik
            processDataForChart(weeklyData)
        }
    }

    private fun processDataForChart(weeklyData: List<SummaryEntity>) {
        val today = LocalDate.now()
        val locale = Locale.forLanguageTag("id-ID")

        val dateSlots = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        val xAxisLabels = dateSlots.map {
            it.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        }


        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>()

        val todayColor = "#C89632".toColorInt()
        val previousDaysColor = "#F4EAD6".toColorInt()

        dateSlots.forEachIndexed { index, date ->
            val dataForDay = weeklyData.find {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) == date
            }

            val fatAmount = dataForDay?.carbs ?: 0.0
            entries.add(
                BarEntry(index.toFloat(), fatAmount.toFloat())
            )

            if (date == today) {
                barColors.add(todayColor)
            } else {
                barColors.add(previousDaysColor)
            }
        }

        val dataSet = BarDataSet(entries, "Konsumsi Gula Mingguan")
        dataSet.setDrawValues(false)
        dataSet.colors = barColors
        dataSet.isHighlightEnabled = false
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        _uiState.value = WeeklyFatState.Success(barData, xAxisLabels)
    }
}