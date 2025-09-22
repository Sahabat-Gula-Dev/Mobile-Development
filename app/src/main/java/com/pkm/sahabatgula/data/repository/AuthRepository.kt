package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.remote.model.GoogleAuthResponse
import com.pkm.sahabatgula.data.remote.model.LoginRequest
import com.pkm.sahabatgula.data.remote.model.LoginResponse
import com.pkm.sahabatgula.data.remote.model.MyProfile
import com.pkm.sahabatgula.data.remote.model.RegisterResponse
import com.pkm.sahabatgula.data.remote.model.ResendOtpRequest
import com.pkm.sahabatgula.data.remote.model.ResendOtpResponse
import com.pkm.sahabatgula.data.remote.model.RegisterRequest
import com.pkm.sahabatgula.data.remote.model.VerifyOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyOtpResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.runCatching

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val profileDao: ProfileDao
) {

    suspend fun register(username: String, email: String, password: String): Result<RegisterResponse> =
        runCatching {
            val response = apiService.register(RegisterRequest(username, email, password))
            response.body()?: throw IllegalStateException("Response body is null")
        }

    suspend fun resendOtp(email: String): Result<ResendOtpResponse> =
        runCatching {
            val response = apiService.resendOtp(ResendOtpRequest(email))
            response.body()?: throw IllegalStateException("Response body is null")
        }

    suspend fun verifyOtp(email: String, otp: String): Result<VerifyOtpResponse> =
        runCatching {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, otp))
            response.body()?: throw IllegalStateException("Response body is null")
        }


    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val token = loginResponse.data.accessToken
                tokenManager.saveAccessToken(token)


                // ambil dan save profile
                val myProfileResponse = apiService.getMyProfile("Bearer $token")
                if(myProfileResponse.isSuccessful) {
                    val body = myProfileResponse.body()
                    val myProfile = body?.data?.myProfile
                    if(myProfile != null){
                        val profileEntity = myProfile.toProfileEntity()
                        profileDao.upsertProfile(profileEntity)

                    } else {
                        return Resource.Error("Gagal mengambil data profil")
                    }
                }
                Resource.Success(loginResponse)

            } else {
                // Implementasikan error handling yang lebih baik di sini
                Resource.Error("Login gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun googleAuth(request: GoogleAuthRequest): Result<GoogleAuthResponse> {
        return runCatching {
            val response = apiService.googleAuth(request)
            response.body()?: throw IllegalStateException("Response body is null")
        }
    }
}

fun MyProfile.toProfileEntity(): ProfileEntity {
    return ProfileEntity(
        id = this.userId,
        username = this.username,
        email = this.email,
        gender = null,
        age = null,
        height = null,
        weight = null,
        waist_circumference = null,
        blood_pressure = null,
        blood_sugar = null,
        eat_vegetables = null,
        diabetes_family = null,
        activity_level = null,
        risk_index = this.riskIndex,
        bmi_score = this.bmiScore,
        max_calories = this.maxCalories.toInt(),
        max_carbs = this.maxCarbs,
        max_protein = this.maxProtein,
        max_fat = this.maxFat,
        max_sugar = this.maxSugar,
        max_natrium = this.maxNatrium,
        max_fiber = this.maxFiber,
        max_potassium = this.maxPotassium
    )
}

