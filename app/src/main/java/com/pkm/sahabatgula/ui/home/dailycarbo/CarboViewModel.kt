package com.pkm.sahabatgula.ui.home.dailycarbo

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

sealed class CarboState{
    object Loading: CarboState()
    data class Success(val totalCarbo: Double, val maxCarbo: Double): CarboState()
    data class Error(val message: String): CarboState()
}

@HiltViewModel
class CarboViewModel @Inject constructor(private val homeRepository: HomeRepository): ViewModel() {
    val carboState: StateFlow<CarboState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())) { profile, summary ->
                if (profile == null) {
                    CarboState.Error("Profil tidak ditemukan.")
                } else if (summary == null) {
                    CarboState.Loading
                } else {
                    CarboState.Success(
                        totalCarbo = summary.carbs ?: 0.0,
                        maxCarbo = profile.max_carbs ?: 0.0
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CarboState.Loading
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