package com.pkm.sahabatgula.ui.home.dailyprotein

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

sealed class ProteinState{
    object Loading: ProteinState()
    data class Success(val totalProtein: Double, val maxProtein: Double): ProteinState()
    data class Error(val message: String): ProteinState()
}

@HiltViewModel
class ProteinViewModel @Inject constructor(private val homeRepository: HomeRepository): ViewModel() {
    val proteinState: StateFlow<ProteinState> =
        homeRepository.observeProfile()
            .combine(homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())) { profile, summary ->
                if (profile == null) {
                    ProteinState.Error("Profil tidak ditemukan.")
                } else if (summary == null) {
                    ProteinState.Loading
                } else {
                    ProteinState.Success(
                        totalProtein = summary.protein ?: 0.0,
                        maxProtein = profile.max_protein ?: 0.0
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProteinState.Loading
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