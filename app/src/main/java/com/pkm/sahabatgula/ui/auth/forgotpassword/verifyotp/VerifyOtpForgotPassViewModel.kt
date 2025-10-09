package com.pkm.sahabatgula.ui.auth.forgotpassword.verifyotp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.AuthRepository
import com.pkm.sahabatgula.ui.auth.otpverification.OtpEffect
import com.pkm.sahabatgula.ui.auth.otpverification.OtpViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.timer
import kotlin.text.orEmpty

interface OtpForgotPassState {
    data object Idle : OtpForgotPassState
    data class Ticking(val remaining:Int) : OtpForgotPassState
    data object ReadyToResend : OtpForgotPassState
    data object Loading : OtpForgotPassState
}

sealed interface OtpForgotPassEffect {
    data class ShowToast(val message: String): OtpForgotPassEffect
    data class VerificationSuccess(val resetToken: String?): OtpForgotPassEffect

}
@HiltViewModel
class VerifyOtpForgotPassViewModel @Inject constructor(
    private val saved: SavedStateHandle, private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OtpForgotPassState>(OtpForgotPassState.Idle)
    val uiState: StateFlow<OtpForgotPassState> = _uiState.asStateFlow()
    private val _effect = MutableSharedFlow<OtpForgotPassEffect>()
    val effect = _effect.asSharedFlow()

    var email: String = ""
        private set

    fun setEmail(emailFromArgs: String) {
        this.email = emailFromArgs
    }

    private var timerJob: Job? = null

    fun verifyOtpReset(email: String, otp: String) {
        if (email.isEmpty()) {
            viewModelScope.launch {
                _effect.emit(OtpForgotPassEffect.ShowToast("Email tidak ditemukan"))
                return@launch
            }
        }
        viewModelScope.launch {
            _uiState.value = OtpForgotPassState.Loading
            when (val result = authRepository.verifyResetOtp(email, otp)) {
                is Resource.Success -> {
                    val resetToken = result.data?.data?.resetToken
                    _effect.emit(OtpForgotPassEffect.VerificationSuccess(resetToken))
                    timerJob?.cancel()
                    _uiState.value = OtpForgotPassState.Idle
                }

                is Resource.Error -> {
                    _effect.emit(
                        OtpForgotPassEffect.ShowToast(
                            result.message ?: "Verifikasi gagal"
                        )
                    )
                    if (timerJob?.isActive != true) _uiState.value =
                        OtpForgotPassState.ReadyToResend
                }

                is Resource.Loading -> {}
            }
        }
    }

    fun startTimer(total: Int = 30) = viewModelScope.launch {
        timerJob?.cancel() // batal jika timer sebelumnya ada
        timerJob = viewModelScope.launch {
            for (s in total - 1 downTo 1) {
                delay(1000)
                _uiState.value = OtpForgotPassState.Ticking(s)
            }
            _uiState.value = OtpForgotPassState.ReadyToResend
        }
    }

    fun resendOtp() = viewModelScope.launch {
        if (email.isEmpty()) {
            viewModelScope.launch {
                _effect.emit(OtpForgotPassEffect.ShowToast("Email tidak ditemukan"))
                return@launch
            }
        }
        viewModelScope.launch {
            if (_uiState.value is OtpForgotPassState.Loading) return@launch
            _uiState.value = OtpForgotPassState.Loading
            val r = authRepository.resendOtp(email)
            r.fold(
                onSuccess = {
                    _effect.emit(OtpForgotPassEffect.ShowToast(it.message))
                    startTimer()
                },
                onFailure = {
                    _effect.emit(OtpForgotPassEffect.ShowToast(it.message ?: "Gagal kirim ulang OTP"))
                    _uiState.value = OtpForgotPassState.ReadyToResend
                }
            )
        }
    }
}