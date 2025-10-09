package com.pkm.sahabatgula.ui.settings.userprofile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed class UserProfileState {
    object Loading : UserProfileState()
    data class Success(
        val height: Int?,
        val weight: Int?,
        val bmi: Double?,
        val diabetesRiskIndex: Int?
    ) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    val userProfileState: StateFlow<UserProfileState> =
        homeRepository.observeProfileEntity()
            .mapNotNull { entity ->
                if (entity == null) {
                    UserProfileState.Loading
                } else {
                    val height = entity.height
                    val weight = entity.weight
                    val bmi = entity.bmi_score
                    val riskIndex = entity.risk_index
                    UserProfileState.Success(height, weight, bmi, riskIndex)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserProfileState.Loading
            )
}