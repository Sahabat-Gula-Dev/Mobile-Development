package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword


sealed interface ResetPasswordState {
    data object Idle : ResetPasswordState
    data object Loading : ResetPasswordState
    data class Success(val message: String) : ResetPasswordState
    data class Error(val message: String) : ResetPasswordState
}


sealed interface ResetPasswordEffect {
    data object NavigateToLogin: ResetPasswordEffect
}