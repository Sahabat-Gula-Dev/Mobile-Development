package com.pkm.sahabatgula.ui.auth.forgotpassword.verifyotp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
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

interface OtpForgotPassViewState {
    data object Idle : OtpForgotPassViewState
    data class Ticking(val remaining: Int) : OtpForgotPassViewState
    data object ReadyToResend : OtpForgotPassViewState
    data object Loading : OtpForgotPassViewState
}

sealed interface OtpForgotPassEffect {
    data class ShowVerifyError(val message: String) : OtpForgotPassEffect
    data class ShowResendInfo(val message: String) : OtpForgotPassEffect
    data class VerificationSuccess(val resetToken: String?) : OtpForgotPassEffect
}

@HiltViewModel
class VerifyOtpForgotPassViewModel @Inject constructor(
    private val saved: SavedStateHandle,
    private val repository: AuthRepository
) : ViewModel() {

    var email: String
        get() = saved.get<String>("email").orEmpty()
        set(value) { saved.set("email", value) }

    private val _ui = MutableStateFlow<OtpForgotPassViewState>(OtpForgotPassViewState.Idle)
    val ui: StateFlow<OtpForgotPassViewState> = _ui

    private val _effect: Channel<OtpForgotPassEffect> = Channel()
    val effect: Flow<OtpForgotPassEffect> = _effect.receiveAsFlow()

    /** Mulai countdown timer 30 detik */
    fun startTimer(total: Int = 30) = viewModelScope.launch {
        _ui.value = OtpForgotPassViewState.Ticking(total)
        for (s in total - 1 downTo 1) {
            delay(1000)
            _ui.value = OtpForgotPassViewState.Ticking(s)
        }
        _ui.value = OtpForgotPassViewState.ReadyToResend
    }

    /** Kirim ulang OTP */
    fun resendOtp() = viewModelScope.launch {
        if (_ui.value is OtpForgotPassViewState.Loading) return@launch
        _ui.value = OtpForgotPassViewState.Loading
        val r = repository.resendOtp(email)
        r.fold(
            onSuccess = {
                _effect.send(OtpForgotPassEffect.ShowResendInfo(it.message))
                startTimer()
            },
            onFailure = {
                _effect.send(OtpForgotPassEffect.ShowVerifyError(it.message ?: "Gagal kirim ulang OTP"))
                _ui.value = OtpForgotPassViewState.ReadyToResend
            }
        )
    }

    /** Verifikasi kode OTP untuk reset password */
    fun verify(otpCode: String) = viewModelScope.launch {
        _ui.value = OtpForgotPassViewState.Loading
        val resource = repository.verifyResetOtp(email, otpCode)
        when (resource) {
            is Resource.Success -> {
                val resetToken = resource.data?.data?.resetToken
                _effect.send(OtpForgotPassEffect.VerificationSuccess(resetToken))
            }
            is Resource.Error -> {
                _effect.send(OtpForgotPassEffect.ShowVerifyError(resource.message ?: "Verifikasi gagal"))
                _ui.value = OtpForgotPassViewState.ReadyToResend
            }
            is Resource.Loading<*> -> Unit
        }
    }
}
