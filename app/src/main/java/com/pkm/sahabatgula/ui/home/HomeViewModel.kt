package com.pkm.sahabatgula.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.room.DailySummaryEntity
import com.pkm.sahabatgula.data.remote.model.DailySummaryResponse
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val homeRepository: HomeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        fetchHOmeData()
    }

    fun todayDate(): String {
        val today = LocalDate.now(ZoneId.systemDefault())
        return today.format(DateTimeFormatter.ISO_DATE) // hasil: "2025-09-24"
    }

    val summary: StateFlow<DailySummaryEntity> =
        homeRepository.observeDailySummary(todayDate())
            .stateIn(viewModelScope, SharingStarted.Lazily, null) as StateFlow<DailySummaryEntity>

    fun refresh() {
        viewModelScope.launch {
            homeRepository.refreshDailySummary()
        }
    }

    fun fetchHOmeData() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading
            val profile = homeRepository.getProfile()

            when (val summaryResponse = homeRepository.getDailySummary()) {
                is Resource.Success -> {
                    val summary = summaryResponse.data!!
                    // Simpan ke Room
                    homeRepository.refreshDailySummary()
                    // UI tetap dapat data terbaru juga
                    _uiState.value = HomeState.Success(profile, summary)
                }
                is Resource.Error -> {
                    _uiState.value = HomeState.Error(summaryResponse.message ?: "Terjadi Kesalahan")
                }
                else -> {}
            }
        }
    }
}