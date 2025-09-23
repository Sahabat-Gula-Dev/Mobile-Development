package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword

interface ResetPasswordState {
    data object Idle: ResetPasswordState
    data object Loading: ResetPasswordState
    data object Success: ResetPasswordState
}

sealed interface ResetPasswordEffect {
    data class ShowToast(val message: String?): ResetPasswordEffect
    data object NavigateToLogin: ResetPasswordEffect
    data object NavigateToHome: ResetPasswordEffect
}