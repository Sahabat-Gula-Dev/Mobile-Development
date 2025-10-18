package com.pkm.sahabatgula.ui.home.dailyactivity.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ActivityState {
    object Loading: ActivityState()
    data class Success(val burned: Int?, val maxCalories: Int?): ActivityState() {
        val maxBurned = maxCalories?.div(2)
    }
    data class Error(val message: String): ActivityState()
}

@HiltViewModel
class ActivityViewModel @Inject constructor(private val homeRepository: HomeRepository): ViewModel() {

    val activityState = combine(
        homeRepository.observeProfile()
            .onStart { emit(null) },
        homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())
            .onStart { emit(null) }
    ) { profile, summary ->
        if (profile == null) {
            ActivityState.Error("Profil tidak ditemukan.")
        } else if (summary == null) {
            ActivityState.Loading
        } else {
            ActivityState.Success(
                burned = summary.burned ?: 0,
                maxCalories = profile.max_calories ?: 0
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActivityState.Loading
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