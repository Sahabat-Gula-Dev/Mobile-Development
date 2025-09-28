package com.pkm.sahabatgula.ui.auth.register

sealed interface RegisterViewState {
    data object Idle : RegisterViewState
    data object Loading : RegisterViewState
    data class Error(val message: String) : RegisterViewState

}

// effect digunakan untuk action yang tidak berlaku terus-menerus, contohnya toast ketika menekan tombol kembali saat register sukses
sealed interface RegisterEffect{
    data class ShowToast(val message: String): RegisterEffect
    data class NavigateToOtpVerification(val email: String): RegisterEffect
    data object NavigateToHome : RegisterEffect
}