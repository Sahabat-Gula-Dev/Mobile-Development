package com.pkm.sahabatgula.ui.auth.otpverification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class OtpVerificationRegisterViewModel @Inject constructor(
    private val saved: SavedStateHandle, private val repository: AuthRepository
) : ViewModel() {

    var email: String
        get() = saved.get<String>("email").orEmpty()
        set(value) { saved.set("email", value) }

    private val _ui = MutableStateFlow<OtpViewState>(OtpViewState.Idle)
    val ui: StateFlow<OtpViewState> = _ui

    private val _effect: Channel<OtpEffect> = Channel()
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    fun startTimer(total: Int = 30) = viewModelScope.launch {
        _ui.value = OtpViewState.Ticking(total)
        for (s in total - 1 downTo 1) {
            delay(1000)
            _ui.value = OtpViewState.Ticking(s)
        }
        _ui.value = OtpViewState.ReadyToResend
    }

    fun resendOtp() = viewModelScope.launch {
        if (_ui.value is OtpViewState.Loading) return@launch
        _ui.value = OtpViewState.Loading
        val r = repository.resendOtp(email)
        r.fold(
            onSuccess = {
                _effect.send(OtpEffect.ShowToast(it.message))
                startTimer() // sukses -> mulai hitung lagi
            },
            onFailure = {
                _effect.send(OtpEffect.ShowToast(it.message ?: "Gagal kirim ulang OTP"))
                _ui.value = OtpViewState.ReadyToResend
            }
        )
    }

    fun verify(code: String) = viewModelScope.launch {
        if (code.length != 6) {
            _effect.send(OtpEffect.ShowToast("Kode OTP harus 6 digit"))
            return@launch
        }
        _ui.value = OtpViewState.Loading
        val r = repository.verifyOtp(email, code)
        r.fold(
            onSuccess = { verifyOtpResponse ->
                val token = verifyOtpResponse.data.accessToken
                _effect.send(OtpEffect.VerificationSuccess(token))
            },
            onFailure = {
                _effect.send(OtpEffect.ShowToast(it.message ?: "Verifikasi gagal"))
                _ui.value = OtpViewState.ReadyToResend
            }
        )
    }
}

