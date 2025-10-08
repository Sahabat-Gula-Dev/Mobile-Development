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


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
): ViewModel() {

    private val _loginState = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    val loginState = _loginState.asStateFlow()
    private val _effect : Channel<LoginEffect> = Channel()
    val effect = _effect.receiveAsFlow()



    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginViewState.Loading
            val resource = repository.login(email, password)
            when (resource) {
                is Resource.Success -> {
                    _loginState.value = LoginViewState.Success(resource.data)
                    val accessToken = tokenManager.getAccessToken()
                    val profile = repository.getMyProfile(accessToken)
                    val bmi = profile?.bmiScore
                    Log.d("RegisterViewModel", " BMI: $bmi")

                    if ( bmi == null || bmi == 0.0) {
                        _effect.send(LoginEffect.NavigateToWelcome)
                    } else {
                        _effect.send(LoginEffect.NavigateToHome)
                    }
                }
                is Resource.Error -> {
                    _loginState.value = LoginViewState.Error(resource.message)
                }
                is Resource.Loading<*> -> {
                    Log.d("LoginViewModel", "login: tunggu dulu")
                }
            }
        }
    }


    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginViewState.Loading

            repository.googleAuth(GoogleAuthRequest(idToken))
                .onSuccess { response ->
                    val accessToken = response.data.accessToken
                    if (accessToken.isNullOrEmpty()) {
                        _loginState.value = Error("Token tidak valid dari server") as LoginViewState
                        _effect.send(LoginEffect.ShowToast("Token tidak valid dari server"))
                        return@onSuccess
                    }

                    tokenManager.saveAccessToken(accessToken)

                    try {
                        val profile = repository.getMyProfile(accessToken)
                        val bmi = profile?.bmiScore
                        Log.d("RegisterViewModel", " BMI: $bmi")

                        if ( bmi == null || bmi == 0.0) {
                            _effect.send(LoginEffect.NavigateToWelcome)
                        } else {
                            _effect.send(LoginEffect.ShowToast("Berhasil masuk dengan Google"))
                            _effect.send(LoginEffect.NavigateToHome)
                        }

                        _loginState.value = LoginViewState.Idle

                    } catch (e: Exception) {
                        _effect.send(LoginEffect.ShowToast("Gagal mengambil data profil: ${e.message}"))
                    }

                    _loginState.value = LoginViewState.Idle
                }
                .onFailure { e ->
                    _loginState.value = LoginViewState.Error(e.message ?: "Gagal login dengan Google")
                    _effect.send(LoginEffect.ShowToast(e.message ?: "Gagal login dengan Google"))
                }
        }
    }
}



