package com.pkm.sahabatgula.data.remote.model

data class VerifyResetOtpResponse(
    val status: String,
    val message: String,
    val data: ResetOtpData
)

data class ResetOtpData(
    val resetToken: String
)

data class VerifyResetOtpRequest(
    val email: String,
    val otp: String
)