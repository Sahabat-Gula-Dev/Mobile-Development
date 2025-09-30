package com.pkm.sahabatgula.ui.home.dailyfat

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

sealed class FatState{
    object Loading: FatState()
    data class Success(val totalFat: Double, val maxFat: Double): FatState() {
        val remainingFat: Double
            get() = maxFat - totalFat
    }
    data class Error(val message: String): FatState()
}

@HiltViewModel
class FatViewModel @Inject constructor(private val homeRepository: HomeRepository): ViewModel() {
    val fatState: StateFlow<FatState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())) { profile, summary ->
                if (profile == null) {
                    FatState.Error("Profil tidak ditemukan.")
                } else if (summary == null) {
                    FatState.Loading
                } else {
                    FatState.Success(
                        totalFat = summary.fat ?: 0.0,
                        maxFat = profile.max_fat ?: 0.0
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FatState.Loading
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