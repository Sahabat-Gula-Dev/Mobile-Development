package com.pkm.sahabatgula.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.remote.model.SummaryActivities
import com.pkm.sahabatgula.data.remote.model.SummaryDaily
import com.pkm.sahabatgula.data.remote.model.SummaryData
import com.pkm.sahabatgula.data.remote.model.SummaryNutrients
import com.pkm.sahabatgula.data.remote.model.SummaryResponse
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val homeRepository: HomeRepository) : ViewModel() {

    private val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

    private val profileFlow = homeRepository.observeProfile()
    private val summaryFlow = homeRepository.observeDailySummary(todayDate)

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
//    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    // Menggabungkan kedua aliran data menjadi satu UI State
    val uiState: StateFlow<HomeState> =
        profileFlow.combine(summaryFlow) { profile, summary ->
            if (profile != null && summary != null) {
                // Skenario ideal: profile dan summary ada di database
                Log.d("HomeViewModel", "State changed to Success from DB. Profile: $profile, Summary: $summary")
                HomeState.Success(profile, summary.toResponse()) // Ubah entity kembali ke response DTO jika diperlukan UI
            } else if (profile != null && summary == null) {
                // Skenario: Profile ada, tapi data hari ini belum ada. Tetap loading sambil menunggu refresh.
                Log.d("HomeViewModel", "Profile found, summary for today is null. Still loading.")
                HomeState.Loading
            } else {
                // Skenario: Profil belum ada sama sekali
                Log.e("HomeViewModel", "Profile not found in DB.")
                HomeState.Error("Profil pengguna tidak ditemukan. Silakan login ulang.")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState.Loading // Nilai awal saat Flow belum memancarkan data
        )


    init {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Attempting to refresh data from network...")
            when (val result = homeRepository.refreshDailySummary()) {
                is Resource.Success -> {
                    Log.d("HomeViewModel", "Data refreshed successfully.")
                    // Tidak perlu mengubah state di sini, karena DB yang terupdate
                    // akan otomatis memicu perubahan pada `uiState` Flow.
                }
                is Resource.Error -> {
                    // Kirim pesan error sebagai effect, agar tidak mengganti state sukses (data lama)
                    Log.e("HomeViewModel", "Failed to refresh data: ${result.message}")
                    _effect.emit(HomeEffect.ShowToast(result.message ?: "Gagal memperbarui data"))
                }
                else -> {}
            }
        }
    }


    fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading
            Log.d("HomeViewModel", "State changed to Loading")

            // Ambil profil terlebih dahulu
            val profile = homeRepository.getProfile()
            if (profile == null) {
                _uiState.value = HomeState.Error("Profil pengguna tidak ditemukan.")
                Log.e("HomeViewModel", "Profile is null, stopping.")
                return@launch
            }

            // Ambil ringkasan harian dari API
            when (val summaryResponse = homeRepository.getSummary()) {
                is Resource.Success -> {
                    // Jika sukses, langsung kirim data (profile dan summary) ke UI
                    val summaryData = summaryResponse.data!!
                    _uiState.value = HomeState.Success(profile, summaryData)
                    Log.d("HomeViewModel", "State changed to Success. Data: $summaryData")
                }
                is Resource.Error -> {
                    _uiState.value = HomeState.Error(summaryResponse.message ?: "Terjadi Kesalahan")
                    Log.e("HomeViewModel", "State changed to Error: ${summaryResponse.message}")
                }
                is Resource.Loading -> { /* Diabaikan karena kita sudah set Loading di awal */ }
            }
        }
    }
}

fun SummaryEntity.toResponse(): SummaryResponse {
    return SummaryResponse(
        status = "success_from_local",
        data = SummaryData(
            daily = SummaryDaily(
                date = date,
                steps = steps,
                water = water,
                nutrients = SummaryNutrients(
                    calories = calories,
                    carbs = carbs,
                    protein = protein,
                    fat = fat,
                    sugar = sugar,
                    sodium = sodium,
                    fiber = fiber,
                    potassium = potassium
                ),
                activities = SummaryActivities(
                    burned = burned
                )
            ),
            weekly = null,   // biarin null karena ga ada data di entity ini
            monthly = null   // sama, null
        )
    )
}

