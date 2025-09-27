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
import com.pkm.sahabatgula.core.utils.DateConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val todayDate = DateConverter.getTodayLocalFormatted()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    val uiState: StateFlow<HomeState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(todayDate)) { profile, todaySummary ->
                Log.d("HomeVM", "Combine: profile=${profile != null}, today=${todaySummary?.date}")

                if (profile == null) {
                    HomeState.Error("Profil tidak ditemukan")
                } else {
                    if (todaySummary != null) {
                        HomeState.Success(profile, todaySummary.toResponse())
                    } else {
                        // fallback default kosong
                        HomeState.Success(profile, createDefaultSummary(todayDate).toResponse())
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeState.Loading
            )

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val result = homeRepository.refreshDailySummary()
            if (result is Resource.Error) {
                _effect.emit(HomeEffect.ShowToast(result.message ?: "Gagal update data"))
            }
        }
    }

    private fun createDefaultSummary(date: String): SummaryEntity {
        return SummaryEntity(
            type = "DAILY",
            date = date,
            calories = 0.0,
            carbs = 0.0,
            protein = 0.0,
            fat = 0.0,
            sugar = 0.0,
            sodium = 0.0,
            fiber = 0.0,
            potassium = 0.0,
            burned = 0,
            steps = 0,
            water = 0
        )
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
            weekly = null,
            monthly = null
        )
    )
}

