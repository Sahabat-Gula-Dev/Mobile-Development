package com.pkm.sahabatgula.data.remote.api

import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.remote.model.GoogleAuthResponse
import com.pkm.sahabatgula.data.remote.model.LoginRequest
import com.pkm.sahabatgula.data.remote.model.LoginResponse
import com.pkm.sahabatgula.data.remote.model.MyProfileResponse
import com.pkm.sahabatgula.data.remote.model.ProfileData
import com.pkm.sahabatgula.data.remote.model.SetupProfileResponse
import com.pkm.sahabatgula.data.remote.model.RegisterRequest
import com.pkm.sahabatgula.data.remote.model.RegisterResponse
import com.pkm.sahabatgula.data.remote.model.ResendOtpRequest
import com.pkm.sahabatgula.data.remote.model.ResendOtpResponse
import com.pkm.sahabatgula.data.remote.model.VerifyOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    suspend fun register(@Body userDataItem: RegisterRequest): Response<RegisterResponse>

    @POST("resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequest): Response<ResendOtpResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("google")
    suspend fun googleAuth(@Body body: GoogleAuthRequest): Response<GoogleAuthResponse>

    @POST ("setup")
    suspend fun setupProfile(
        @Header("Authorization") token: String,
        @Body profileData: ProfileData
    ): Response<SetupProfileResponse>

    @GET("me")
    suspend fun getMyProfile(@Header("Authorization") token: String): Response<MyProfileResponse>
}