package com.pkm.sahabatgula.ui.home.dailysugar.history.monthly


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
import java.time.YearMonth
import java.time.ZoneId


// Sealed class untuk menampung state UI, termasuk data grafik

sealed class MonthlySugarState {
    object Loading : MonthlySugarState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : MonthlySugarState()
    data class Error(val message: String) : MonthlySugarState()
}


@HiltViewModel
class MonthlySugarViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthlySugarState>(MonthlySugarState.Loading)
    val uiState: StateFlow<MonthlySugarState> = _uiState

    init {
        loadMonthlySugarData()
    }

    private fun loadMonthlySugarData() {
        viewModelScope.launch {
            _uiState.value = MonthlySugarState.Loading

            // Ambil data bulanan dari repository
            val monthlyData = homeRepository.observeMonthlySummary().firstOrNull()

            if (monthlyData.isNullOrEmpty()) {
                _uiState.value = MonthlySugarState.Error("Data bulanan tidak ditemukan.")
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

        val currentMonthColor = "#FF3776".toColorInt()
        val previousMonthColor = "#FFDFE9".toColorInt()

        monthSlots.forEachIndexed { index, yearMonth ->
            // Format "yyyy-MM" untuk mencocokkan dengan data dari API/DB
            val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

            // Cari data yang cocok dengan slot bulan
            val dataForMonth = monthlyData.find { entity ->
                // Pastikan date tidak null dan parse ke YearMonth
                entity.date.let { YearMonth.parse(it, monthFormatter) } == yearMonth
            }

            val sugarAmount = dataForMonth?.sugar ?: 0.0
            entries.add(BarEntry(index.toFloat(), sugarAmount.toFloat()))

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

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        _uiState.value = MonthlySugarState.Success(barData, xAxisLabels)
    }
}