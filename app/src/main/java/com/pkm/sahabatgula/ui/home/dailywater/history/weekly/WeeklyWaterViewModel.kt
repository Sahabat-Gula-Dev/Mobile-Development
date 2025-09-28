package com.pkm.sahabatgula.ui.home.dailywater.history.weekly


import androidx.core.graphics.toColorInt
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


// Sealed class untuk menampung state UI, termasuk data grafik
sealed class WeeklyWaterState {
    object Loading : WeeklyWaterState()
    data class Success(
        val barData: BarData,
        val xAxisLabels: List<String>
    ) : WeeklyWaterState()
    data class Error(val message: String) : WeeklyWaterState()
}

@HiltViewModel
class WeeklyWaterViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeeklyWaterState>(WeeklyWaterState.Loading)
    val uiState: StateFlow<WeeklyWaterState> = _uiState

    init {
        loadWeeklyWaterData()
    }

    private fun loadWeeklyWaterData() {
        viewModelScope.launch {
            _uiState.value = WeeklyWaterState.Loading

            // Ambil data mingguan dari database (melalui Flow)
            // .firstOrNull() mengambil nilai saat ini dari Flow sekali saja
            val weeklyData = homeRepository.observeWeeklySummary().firstOrNull()

            if (weeklyData.isNullOrEmpty()) {
                _uiState.value = WeeklyWaterState.Error("Data mingguan tidak ditemukan.")
                return@launch
            }

            // Proses data untuk grafik
            processDataForChart(weeklyData)
        }
    }

    private fun processDataForChart(weeklyData: List<SummaryEntity>) {
        val today = LocalDate.now()
        val locale = Locale("id", "ID")

        val dateSlots = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        val xAxisLabels = dateSlots.map {
            it.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        }

        // Siapkan BarEntry dan daftar warna
        val entries = ArrayList<BarEntry>()
        val barColors = ArrayList<Int>() // <-- 1. Buat daftar kosong untuk warna

        // Definisikan warna yang Anda inginkan
        val todayColor = "#2196F3".toColorInt()
        val previousDaysColor = "#D3EAFD".toColorInt()


        dateSlots.forEachIndexed { index, date ->
            val dataForDay = weeklyData.find {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) == date
            }

            val waterAmount = dataForDay?.water ?: 0
            entries.add(
                BarEntry(index.toFloat(), waterAmount.toFloat())
            )

            // --- LOGIKA PEWARNAAN DINAMIS ---
            // 2. Cek apakah tanggal saat ini adalah 'today'
            if (date == today) {
                barColors.add(todayColor) // Jika ya, gunakan warna "hari ini"
            } else {
                barColors.add(previousDaysColor) // Jika tidak, gunakan warna "hari sebelumnya"
            }
        }

        val dataSet = BarDataSet(entries, "Asupan Air Mingguan")
        dataSet.setDrawValues(false)
        dataSet.colors = barColors // <-- 3. Terapkan daftar warna ke dataset
        dataSet.isHighlightEnabled = false

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        _uiState.value = WeeklyWaterState.Success(barData, xAxisLabels)
    }
}