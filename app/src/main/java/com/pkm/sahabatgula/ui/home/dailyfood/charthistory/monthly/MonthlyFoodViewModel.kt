package com.pkm.sahabatgula.ui.home.dailyfood.charthistory.monthly


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

sealed class MonthlyFoodState {
    object Loading : MonthlyFoodState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : MonthlyFoodState()
    data class Error(val message: String) : MonthlyFoodState()
}


@HiltViewModel
class MonthlyFoodViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthlyFoodState>(MonthlyFoodState.Loading)
    val uiState: StateFlow<MonthlyFoodState> = _uiState

    init {
        loadMonthlyFoodData()
    }

    private fun loadMonthlyFoodData() {
        viewModelScope.launch {
            _uiState.value = MonthlyFoodState.Loading

            // Ambil data bulanan dari repository
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()

            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlyFoodState.Error("Data bulanan tidak ditemukan.")
                return@launch
            }

            processDataForChart(monthlyData)
        }
    }

    private fun processDataForChart(monthlyData: List<SummaryEntity>) {
        // Menggunakan YearMonth untuk bekerja dengan bulan
        val currentMonth = YearMonth.now()
        val locale = Locale.forLanguageTag("id-ID") // Locale Indonesia untuk nama bulan

        // 1. Buat 7 slot untuk 7 bulan terakhir, diakhiri dengan bulan ini
        val monthSlots = (0..6).map { currentMonth.minusMonths(it.toLong()) }.reversed()
        val xAxisLabels = monthSlots.map {
            it.format(DateTimeFormatter.ofPattern("MMM", locale))
        }

        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>()

        val currentMonthColor = "#088D08".toColorInt()
        val previousMonthColor = "#CEE8CE".toColorInt()

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

        _uiState.value = MonthlyFoodState.Success(barData, xAxisLabels)
    }
}