package com.pkm.sahabatgula.ui.auth.forgotpassword.inputemail

interface ForgotPasswordEmailState {
    data object Idle: ForgotPasswordEmailState
    data object Loading: ForgotPasswordEmailState
    data class Success(val message: String?): ForgotPasswordEmailState
    data class Error(val message: String): ForgotPasswordEmailState

}

sealed class ForgotPasswordEmailEffect {
    data class ShowToast(val message: String): ForgotPasswordEmailEffect()
    data class NavigateToOtpVerification(val email: String): ForgotPasswordEmailEffect()

}