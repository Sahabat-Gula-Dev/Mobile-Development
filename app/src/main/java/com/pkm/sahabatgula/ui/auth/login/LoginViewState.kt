package com.pkm.sahabatgula.ui.auth.login

import com.pkm.sahabatgula.data.remote.model.LoginResponse

sealed interface LoginViewState {
    data object Idle: LoginViewState
    data object Loading: LoginViewState
    data class Success(val data: LoginResponse?): LoginViewState
    data class Error(val message: String?): LoginViewState
}

sealed interface LoginEffect {
    data class ShowToast(val message: String): LoginEffect
    data object NavigateToHome: LoginEffect
}