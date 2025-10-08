package com.pkm.sahabatgula.ui.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch



@HiltViewModel
class RegisterViewModel @Inject constructor(private val repo: AuthRepository, private val tokenManager: TokenManager): ViewModel() {
    private val _registerViewState = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    val registerViewState: StateFlow<RegisterViewState> = _registerViewState

    private val _effect: Channel<RegisterEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading

            val result = repo.register(username, email, password)

            result.fold(
                onSuccess = { response ->
                    _effect.send(RegisterEffect.ShowToast(response.message?: "Registrasi Berhasil"))
                    _effect.send(RegisterEffect.NavigateToOtpVerification(email))
                    _registerViewState.value = RegisterViewState.Idle
                },
                onFailure = { exception ->
                    _effect.send(RegisterEffect.ShowToast(exception.message?: "Registrasi Gagal"))
                    _registerViewState.value = RegisterViewState.Error(exception.message?: "Registrasi Gagal")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading

            repo.googleAuth(GoogleAuthRequest(idToken))
                .onSuccess { response ->
                    _effect.send(RegisterEffect.ShowToast("Berhasil masuk dengan Google"))
                    val accessToken = response.data?.accessToken
                    if (accessToken.isNullOrEmpty()) {
                        _effect.send(RegisterEffect.ShowToast("Token tidak valid dari server"))
                        _registerViewState.value = RegisterViewState.Error("Token tidak valid dari server")
                        return@onSuccess
                    }

                    tokenManager.saveAccessToken(accessToken)

                    try {
                        val profile = repo.getMyProfile(accessToken)
                        val bmi = profile?.bmiScore
                        Log.d("RegisterViewModel", "BMI: $bmi")

                        if ( bmi == null || bmi == 0.0) {

                            _effect.send(RegisterEffect.NavigateToWelcomeScreen)
                        } else {
                            _effect.send(RegisterEffect.NavigateToHome)
                        }

                    _registerViewState.value = RegisterViewState.Idle

                    } catch (e: Exception) {
                        _effect.send(RegisterEffect.ShowToast("Gagal mengambil data profil: ${e.message}"))
                        _registerViewState.value = RegisterViewState.Error("Gagal mengambil data profil")
                    }
                }
                .onFailure { authError ->
                    _effect.send(RegisterEffect.ShowToast(authError.message ?: "Gagal masuk dengan Google"))
                    _registerViewState.value = RegisterViewState.Error(authError.message ?: "Gagal masuk dengan Google")
                }
        }
    }
}