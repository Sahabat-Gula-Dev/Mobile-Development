package com.pkm.sahabatgula.ui.auth.forgotpassword.verifyotp

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
