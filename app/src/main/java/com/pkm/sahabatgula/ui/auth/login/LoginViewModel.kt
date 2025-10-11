package com.pkm.sahabatgula.ui.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pkm.sahabatgula.data.remote.model.LoginResponse

sealed interface LoginViewState {
    data object Idle : LoginViewState
    data object Loading : LoginViewState
    data class Success(val data: LoginResponse?) : LoginViewState
    data class Error(val message: String?) : LoginViewState
}

sealed interface LoginEffect {
    object NavigateToHome : LoginEffect
    object NavigateToWelcome : LoginEffect
}

enum class LoginMode {
    EMAIL,
    GOOGLE,
    NONE
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _effect: Channel<LoginEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private val _loginMode = MutableStateFlow(LoginMode.NONE)
    val loginMode = _loginMode.asStateFlow()

    private var _pendingNavigation: LoginEffect? = null

    fun login(email: String, password: String) {
        _loginMode.value = LoginMode.EMAIL
        viewModelScope.launch {
            _loginState.value = LoginViewState.Loading
            when (val resource = repository.login(email, password)) {
                is Resource.Success -> {
                    _loginState.value = LoginViewState.Success(resource.data)
                    val accessToken = tokenManager.getAccessToken()
                    val profile = repository.getMyProfile(accessToken)
                    val bmi = profile?.bmiScore
                    _pendingNavigation = if (bmi == null || bmi == 0.0) {
                        LoginEffect.NavigateToWelcome
                    } else {
                        LoginEffect.NavigateToHome
                    }
                }
                is Resource.Error -> {
                    _loginState.value = LoginViewState.Error(resource.message)
                }
                else -> {}
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _loginMode.value = LoginMode.GOOGLE
        viewModelScope.launch {
            _loginState.value = LoginViewState.Loading
            repository.googleAuth(GoogleAuthRequest(idToken))
                .onSuccess { response ->
                    val accessToken = response.data.accessToken
                    if (accessToken.isNullOrEmpty()) {
                        _loginState.value = LoginViewState.Error("Token tidak valid dari server")
                        return@onSuccess
                    }
                    tokenManager.saveAccessToken(accessToken)
                    val profile = repository.getMyProfile(accessToken)
                    val bmi = profile?.bmiScore
                    _pendingNavigation = if (bmi == null || bmi == 0.0) {
                        LoginEffect.NavigateToWelcome
                    } else {
                        LoginEffect.NavigateToHome
                    }
                    _loginState.value = LoginViewState.Success(null)
                }
                .onFailure { e ->
                    _loginState.value = LoginViewState.Error(e.message ?: "Gagal login dengan Google")
                }
        }
    }

    fun consumePendingNavigation(): LoginEffect? {
        val effect = _pendingNavigation
        _pendingNavigation = null
        _loginMode.value = LoginMode.NONE
        return effect
    }
}
