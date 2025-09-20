package com.pkm.sahabatgula.data.remote.model

// otp response
data class ResendOtpRequest(val email: String)
data class VerifyOtpRequest(val email: String, val otp: String)

data class ResendOtpResponse(
    val status: String,
    val message: String)

data class VerifyOtpResponse (
    val status: String,
    val message: String,
    val data: TokenData,
)

data class TokenData(val accessToken: String)

// google auth
data class GoogleAuthRequest (val idToken: String?)

data class GoogleAuthResponse(
    val status: String,
    val message: String,
    val data: TokenData
)

// register response
data class RegisterResponse(
    val status: String,
    val message: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)


// login response
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val data: TokenData,
)