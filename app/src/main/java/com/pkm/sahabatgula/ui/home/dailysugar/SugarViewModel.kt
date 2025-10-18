package com.pkm.sahabatgula.ui.home.dailysugar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.repository.HomeRepository
import com.pkm.sahabatgula.core.utils.DateConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SugarViewModel @Inject constructor(homeRepository: HomeRepository): ViewModel(){

    val sugarState: StateFlow<SugarState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())) { profile, summary ->

                if (profile == null) {
                    SugarState.Error("Profil tidak ditemukan.")
                } else {
                    SugarState.Success(
                        currentSugar = summary?.sugar ?: 0.0,
                        maxSugar = profile.max_sugar ?: 0.0
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SugarState.Loading
            )

    init {
        refreshData(homeRepository)
    }

    private fun refreshData(homeRepository: HomeRepository) {
        viewModelScope.launch {
            homeRepository.refreshDailySummary()
        }
    }


}