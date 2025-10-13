package com.pkm.sahabatgula.ui.home.dailywater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WaterState {
    object  Loading: WaterState()
    data class Success(
        val filledGlasses: Int = 0,
        val maxWater: Int = 2000,
        val waterPerGlass: Int = 250
    ): WaterState() {
        val currentWater: Int get() = filledGlasses.times(waterPerGlass)
        val remainingWater: Int get() = maxWater - currentWater
    }
    data class Error(val message: String): WaterState()
}

@HiltViewModel
class WaterViewModel @Inject constructor(private val homeRepository: HomeRepository) : ViewModel() {

    private val _waterState = MutableStateFlow<WaterState>(WaterState.Loading)
    val waterState = _waterState.asStateFlow()
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()
    private val _logWaterStatus = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val logWaterStatus = _logWaterStatus


    init {
        viewModelScope.launch {
            homeRepository.observeDailySummary(DateConverter.getTodayLocalFormatted())
                .collect { summaryEntity ->
                    if (summaryEntity == null) {
                        if (_waterState.value is WaterState.Loading) {
                            _waterState.value = WaterState.Success(filledGlasses = 0)
                        }
                    } else {
                        val initialFilledGlasses = (summaryEntity.water ?: 0) / 250
                        _waterState.value = WaterState.Success(filledGlasses = initialFilledGlasses)
                    }
                }
        }
        refreshData()
    }

    fun addOneGlassOfWater() {
        val currentState = _waterState.value
        if (currentState is WaterState.Success) {
            val newFilledCount = currentState.filledGlasses + 1
            val originalFilledCount = currentState.filledGlasses
            _waterState.update { currentState.copy(filledGlasses = newFilledCount) }
            viewModelScope.launch {
                _logWaterStatus.value = Resource.Loading()
                val increment = currentState.waterPerGlass
                val result = homeRepository.updateWaterIntake(increment)
                if (result is Resource.Error) {
                    _waterState.update { currentState.copy(filledGlasses = originalFilledCount) }
                    _logWaterStatus.value = Resource.Error(result.message ?: "Gagal mencatat air")
                } else if (result is Resource.Success) {
                    _logWaterStatus.value = Resource.Success(Unit)
                }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            homeRepository.refreshDailySummary()
        }
    }
}