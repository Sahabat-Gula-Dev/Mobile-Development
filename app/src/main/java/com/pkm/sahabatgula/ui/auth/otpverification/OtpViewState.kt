package com.pkm.sahabatgula.ui.auth.otpverification

interface OtpViewState {
    data object Idle : OtpViewState
    data class Ticking(val remaining:Int) : OtpViewState
    data object ReadyToResend : OtpViewState
    data object Loading : OtpViewState
}

sealed interface OtpEffect {
    data class ShowToast(val message: String): OtpEffect
    data class VerificationSuccess(val accessToken: String): OtpEffect
}