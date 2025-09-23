package com.pkm.sahabatgula.data.remote.model

data class ResetPasswordResponse (
    val message: String,
    val status: String
)

data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String,
)
