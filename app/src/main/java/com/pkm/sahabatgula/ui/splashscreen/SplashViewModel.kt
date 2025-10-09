package com.pkm.sahabatgula.ui.splashscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.pkm.sahabatgula.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.utils.isNetworkAvailable
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.repository.AuthRepository
import com.pkm.sahabatgula.data.repository.OnboardingRepository
import com.pkm.sahabatgula.ui.auth.login.LoginEffect
import com.pkm.sahabatgula.ui.auth.login.LoginViewState
import com.pkm.sahabatgula.ui.state.GlobalUiState
import kotlinx.coroutines.launch
import java.io.IOException

enum class SplashDestination {
    ONBOARDING_FLOW,
    AUTH_FLOW,
    WELCOME_FLOW,
    HOME_FLOW,
    LOADING
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val onboardingRepository: OnboardingRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
): ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination = _destination.asStateFlow()

    private val _uiState = MutableStateFlow<GlobalUiState>(GlobalUiState.None)
    val uiState = _uiState.asStateFlow()

    fun checkUserSession() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1200)

                if (onboardingRepository.isFirstTime()) {
                    _destination.value = SplashDestination.ONBOARDING_FLOW
                    return@launch
                }

                if (!sessionManager.isLoggedIn()) {
                    _destination.value = SplashDestination.AUTH_FLOW
                    return@launch
                }

                val accessToken = tokenManager.getAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    _destination.value = SplashDestination.AUTH_FLOW
                    return@launch
                }

                val profile = authRepository.getMyProfile(accessToken)
                val bmi = profile?.bmiScore

                if (bmi == null || bmi == 0.0) {
                    _destination.value = SplashDestination.WELCOME_FLOW
                } else {
                    _destination.value = SplashDestination.HOME_FLOW
                }

            } catch (e: IOException) {
                // Offline mode fallback
                val localProfile = authRepository.getLocalProfile()
                val bmi = localProfile?.bmi_score
                if (bmi == null || bmi == 0.0) {
                    _destination.value = SplashDestination.WELCOME_FLOW
                } else {
                    _destination.value = SplashDestination.HOME_FLOW
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Unexpected error. Logging out for safety.", e)
                sessionManager.clearSession()
                _destination.value = SplashDestination.AUTH_FLOW
            }
        }
    }

}
