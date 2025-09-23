package com.pkm.sahabatgula.data.remote.model

// Request body untuk /forgot-password
data class ForgotPasswordRequest(
    val email: String
)

// Response body untuk /forgot-password
data class ForgotPasswordResponse(
    val status: String,
    val message: String
)