package com.pkm.sahabatgula.ui.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface RegisterViewState {
    data object Idle : RegisterViewState
    data object Loading : RegisterViewState
    data class Error(val message: String) : RegisterViewState
    data object GoogleLoginSuccess : RegisterViewState
}

// effect digunakan untuk action yang tidak berlaku terus-menerus, contohnya toast ketika menekan tombol kembali saat register sukses
sealed interface RegisterEffect {
    data class ShowError(val message: String) : RegisterEffect
    data class ShowInfo(val message: String) : RegisterEffect
    data class ShowSuccess(val message: String) : RegisterEffect
    data class NavigateToOtpVerification(val email: String) : RegisterEffect
    data object NavigateToWelcomeScreen : RegisterEffect
    data object NavigateToHome : RegisterEffect
}


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _registerViewState = MutableStateFlow<RegisterViewState>(RegisterViewState.Idle)
    val registerViewState: StateFlow<RegisterViewState> = _registerViewState

    private val _effect = MutableSharedFlow<RegisterEffect>(replay = 0) // ✅ ubah replay
    val effect = _effect.asSharedFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading
            val result = repo.register(username, email, password)

            result.fold(
                onSuccess = { response ->
                    _effect.emit(RegisterEffect.ShowSuccess(response.message ?: "Registrasi Berhasil"))
                    _effect.emit(RegisterEffect.NavigateToOtpVerification(email))
                    _registerViewState.value = RegisterViewState.Idle
                },
                onFailure = { exception ->
                    _effect.emit(RegisterEffect.ShowError(exception.message ?: "Registrasi Gagal"))
                    _registerViewState.value = RegisterViewState.Error(exception.message ?: "Registrasi Gagal")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading

            repo.googleAuth(GoogleAuthRequest(idToken))
                .onSuccess { response ->
                    val accessToken = response.data?.accessToken
                    if (accessToken.isNullOrEmpty()) {
                        _effect.emit(RegisterEffect.ShowError("Token tidak valid dari server"))
                        _registerViewState.value = RegisterViewState.Error("Token tidak valid dari server")
                        return@onSuccess
                    }

                    tokenManager.saveAccessToken(accessToken)

                    try {
                        val profile = repo.getMyProfile(accessToken)
                        val bmi = profile?.bmiScore
                        Log.d("RegisterViewModel", "BMI: $bmi")

                        _registerViewState.value = RegisterViewState.GoogleLoginSuccess
                        if (bmi == null || bmi == 0.0) {
                            _effect.emit(RegisterEffect.NavigateToWelcomeScreen)
                        } else {
                            _effect.emit(RegisterEffect.NavigateToHome)
                        }

                    } catch (e: Exception) {
                        _effect.emit(RegisterEffect.ShowError("Gagal ambil profil: ${e.message}"))
                        _registerViewState.value = RegisterViewState.Error("Gagal ambil profil")
                    }
                }
                .onFailure { e ->
                    _effect.emit(RegisterEffect.ShowError(e.message ?: "Gagal login Google"))
                    _registerViewState.value = RegisterViewState.Error(e.message ?: "Gagal login Google")
                }
        }
    }
}
