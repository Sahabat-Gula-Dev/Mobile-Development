package com.pkm.sahabatgula.ui.home.dailyfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class FoodState {
    object Loading: FoodState()
    data class Success(val totalCalories: Double, val maxCalories: Int?): FoodState(){
        val remainingCalories: Double get() = maxCalories?.minus(totalCalories) ?: 0.0
    }
    data class Error(val message: String): FoodState()
}

@HiltViewModel
class FoodViewModel @Inject constructor(private val homeRepository: HomeRepository): ViewModel() {

    val foodState: StateFlow<FoodState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())) { profile, summary ->
                if (profile == null) {
                    FoodState.Error("Profil tidak ditemukan.")
                } else if (summary == null) {
                    FoodState.Loading
                } else {
                    FoodState.Success(
                        totalCalories = summary.calories ?: 0.0,
                        maxCalories = profile.max_calories ?: 0
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FoodState.Loading
            )

    init {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            homeRepository.refreshDailySummary()
        }
    }
}