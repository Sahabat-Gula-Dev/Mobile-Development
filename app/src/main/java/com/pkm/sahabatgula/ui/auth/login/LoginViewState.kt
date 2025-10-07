package com.pkm.sahabatgula.ui.auth.login

import com.pkm.sahabatgula.data.remote.model.LoginResponse
import com.pkm.sahabatgula.data.remote.model.TokenData
import com.pkm.sahabatgula.ui.auth.otpverification.OtpViewState

sealed interface LoginViewState {
    data object Idle: LoginViewState
    data object Loading: LoginViewState, OtpViewState
    data class Success(val data: LoginResponse?): LoginViewState, OtpViewState
    data class Error(val message: String?): LoginViewState, OtpViewState
}
