package com.pkm.sahabatgula.ui.home.dailyfood.charthistory.weekly

import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject


sealed class WeeklyFoodState {
    object Loading : WeeklyFoodState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : WeeklyFoodState()
    data class Error(val message: String) : WeeklyFoodState()
}

@HiltViewModel
class WeeklyFoodViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    val uiState: StateFlow<WeeklyFoodState> = homeRepository.observeWeeklySummary()
        .map { weeklyData ->
            processDataForChart(weeklyData)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyFoodState.Loading
        )

    private fun processDataForChart(weeklyData: List<SummaryEntity>): WeeklyFoodState {
        val today = LocalDate.now()
        val locale = Locale.forLanguageTag("id-ID")

        val dateSlots = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        val xAxisLabels = dateSlots.map {
            it.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        }

        if (weeklyData.isEmpty()) {
            val emptyEntries = ArrayList<BarEntry>()
            dateSlots.forEachIndexed { index, _ ->
                emptyEntries.add(BarEntry(index.toFloat(), 0f))
            }
            val emptyDataSet = BarDataSet(emptyEntries, "")
            emptyDataSet.setDrawValues(false)
            emptyDataSet.color = "#CEE8CE".toColorInt()
            val emptyBarData = BarData(emptyDataSet)
            emptyBarData.barWidth = 0.6f
            return WeeklyFoodState.Success(emptyBarData, xAxisLabels)
        }

        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>()

        val todayColor = "#088D08".toColorInt()
        val previousDaysColor = "#CEE8CE".toColorInt()

        dateSlots.forEachIndexed { index, date ->
            val dataForDay = weeklyData.find {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) == date
            }
            val foodAmount = dataForDay?.calories ?: 0.0
            entries.add(BarEntry(index.toFloat(), foodAmount.toFloat()))

            if (date == today) {
                barColors.add(todayColor)
            } else {
                barColors.add(previousDaysColor)
            }
        }

        val dataSet = BarDataSet(entries, "Konsumsi Kalori Mingguan")
        dataSet.setDrawValues(false)
        dataSet.colors = barColors
        dataSet.isHighlightEnabled = false
        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        return WeeklyFoodState.Success(barData, xAxisLabels)
    }
}