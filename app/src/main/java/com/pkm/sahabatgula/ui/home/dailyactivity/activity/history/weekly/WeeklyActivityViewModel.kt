package com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.weekly


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


sealed class WeeklyActivityState {
    object Loading : WeeklyActivityState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : WeeklyActivityState()
    data class Error(val message: String) : WeeklyActivityState()
}

@HiltViewModel
class WeeklyActivityViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeeklyActivityState>(WeeklyActivityState.Loading)
    val uiState: StateFlow<WeeklyActivityState> = _uiState

    init {
        loadWeeklyActivityData()
    }

    private fun loadWeeklyActivityData() {
        viewModelScope.launch {
            _uiState.value = WeeklyActivityState.Loading
            val weeklyData = homeRepository.observeWeeklySummary().firstOrNull()
            if (weeklyData.isNullOrEmpty()) {
                _uiState.value = WeeklyActivityState.Error("Data mingguan tidak ditemukan.")
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

        val todayColor = "#C77504".toColorInt()
        val previousDaysColor = "#F4E3CD".toColorInt()

        dateSlots.forEachIndexed { index, date ->
            val dataForDay = weeklyData.find {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) == date
            }

            val burnedAmount = dataForDay?.burned ?: 0
            entries.add(
                BarEntry(index.toFloat(), burnedAmount.toFloat())
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

        _uiState.value = WeeklyActivityState.Success(barData, xAxisLabels)
    }
}