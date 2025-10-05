package com.pkm.sahabatgula.ui.settings.userprofile

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.utils.RiskCategory
import com.pkm.sahabatgula.core.utils.getRiskCategory
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed class UserProfileState {
    object Loading : UserProfileState()
    data class Success(
        val height: Double?,
        val weight: Double?,
        val bmi: Double?,
        val diabetesRiskIndex: Int?
    ) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val userProfileState: StateFlow<UserProfileState> =
        homeRepository.observeProfileEntity()
            .map { entity ->
                if (entity == null) {
                    UserProfileState.Loading
                } else {
                    val height = entity.height?.toDouble()
                    val weight = entity.weight?.toDouble()
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