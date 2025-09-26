package com.pkm.sahabatgula.ui.home.dailysugar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SugarViewModel @Inject constructor(homeRepository: HomeRepository): ViewModel(){

    private val _sugarState = MutableStateFlow<SugarState>(SugarState.Loading)
    val sugarState: StateFlow<SugarState> = _sugarState

    init{
        val today = LocalDate.now(ZoneId.of("Asia/Makassar"))
            .format(DateTimeFormatter.ISO_DATE)
        val summaryFlow = homeRepository.observeDailySummary(today)
        val profileFlow = homeRepository.observeProfile()

        viewModelScope.launch {
            combine(summaryFlow, profileFlow) { summary, profile ->
                val currentSugar = summary?.sugar ?: 0.0
                val maxSugar = profile?.max_sugar ?: 2000.0
                SugarState.Success(currentSugar, maxSugar)
            }.collect {
                _sugarState.value = it
            }
        }
    }


}