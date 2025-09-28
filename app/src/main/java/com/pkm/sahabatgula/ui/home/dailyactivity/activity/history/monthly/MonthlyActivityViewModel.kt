package com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.monthly


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


// Sealed class untuk menampung state UI, termasuk data grafik

sealed class MonthlyActivityState {
    object Loading : MonthlyActivityState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : MonthlyActivityState()
    data class Error(val message: String) : MonthlyActivityState()
}


@HiltViewModel
class MonthlyActivityViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthlyActivityState>(MonthlyActivityState.Loading)
    val uiState: StateFlow<MonthlyActivityState> = _uiState

    init {
        loadMonthlyActivityData()
    }

    private fun loadMonthlyActivityData() {
        viewModelScope.launch {
            _uiState.value = MonthlyActivityState.Loading

            // Ambil data bulanan dari repository
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()

            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlyActivityState.Error("Data bulanan tidak ditemukan.")
                return@launch
            }

            processDataForChart(monthlyData)
        }
    }

    private fun processDataForChart(monthlyData: List<SummaryEntity>) {
        // Menggunakan YearMonth untuk bekerja dengan bulan
        val currentMonth = YearMonth.now()
        val locale = Locale("id", "ID") // Locale Indonesia untuk nama bulan

        // 1. Buat 7 slot untuk 7 bulan terakhir, diakhiri dengan bulan ini
        val monthSlots = (0..6).map { currentMonth.minusMonths(it.toLong()) }.reversed()
        val xAxisLabels = monthSlots.map {
            it.format(DateTimeFormatter.ofPattern("MMM", locale))
        }

        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>()

        val currentMonthColor = "#C77504".toColorInt()
        val previousMonthColor = "#F4E3CD".toColorInt()

        monthSlots.forEachIndexed { index, yearMonth ->
            // Format "yyyy-MM" untuk mencocokkan dengan data dari API/DB
            val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

            // Cari data yang cocok dengan slot bulan
            val dataForMonth = monthlyData.find { entity ->
                // Pastikan date tidak null dan parse ke YearMonth
                entity.date.let { YearMonth.parse(it, monthFormatter) } == yearMonth
            }

            val caloriesAmount = dataForMonth?.calories ?: 0.0
            entries.add(BarEntry(index.toFloat(), caloriesAmount.toFloat()))

            // Logika pewarnaan dinamis
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

        _uiState.value = MonthlyActivityState.Success(barData, xAxisLabels)
    }
}