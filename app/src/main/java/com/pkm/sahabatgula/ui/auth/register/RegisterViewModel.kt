package com.pkm.sahabatgula.ui.auth.register

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

    // chanel untuk pengiriman effect
    private val _effect: Channel<RegisterEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading

            val result = repo.register(username, email, password)

            result.fold(
                onSuccess = { response ->
                    // jika sukses, kirim effect, bukan ubah state
                    _effect.send(RegisterEffect.ShowToast(response.message?: "Registrasi Berhasil"))
                    _effect.send(RegisterEffect.NavigateToOtpVerification(email))
                    // kembalikan statenya ke idle
                    _registerViewState.value = RegisterViewState.Idle
                },
                onFailure = { exception ->
                    // jika gagal, kirim effect toast dan ubah state ke error
                    _effect.send(RegisterEffect.ShowToast(exception.message?: "Registrasi Gagal"))
                    _registerViewState.value = RegisterViewState.Error(exception.message?: "Registrasi Gagal")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String?){
        viewModelScope.launch {
            _registerViewState.value = RegisterViewState.Loading
            repo.googleAuth(GoogleAuthRequest(idToken))
                .onSuccess { response ->
                    _effect.send(RegisterEffect.ShowToast("Berhasil masuk dengan Google"))
                    _registerViewState.value = RegisterViewState.Idle
                }
                .onFailure { e ->
                    _effect.send(RegisterEffect.ShowToast(e.message ?: "Gagal masuk dengan Google"))
                    _registerViewState.value = RegisterViewState.Error(e.message ?: "Gagal masuk dengan Google")
                }
        }
    }
}