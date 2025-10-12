package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.isProfileCompleted
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.ForgotPasswordRequest
import com.pkm.sahabatgula.data.remote.model.ForgotPasswordResponse
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.remote.model.GoogleAuthResponse
import com.pkm.sahabatgula.data.remote.model.LoginRequest
import com.pkm.sahabatgula.data.remote.model.LoginResponse
import com.pkm.sahabatgula.data.remote.model.MyProfile
import com.pkm.sahabatgula.data.remote.model.RegisterResponse
import com.pkm.sahabatgula.data.remote.model.ResendOtpRequest
import com.pkm.sahabatgula.data.remote.model.ResendOtpResponse
import com.pkm.sahabatgula.data.remote.model.RegisterRequest
import com.pkm.sahabatgula.data.remote.model.ResetPasswordRequest
import com.pkm.sahabatgula.data.remote.model.ResetPasswordResponse
import com.pkm.sahabatgula.data.remote.model.VerifyOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyOtpResponse
import com.pkm.sahabatgula.data.remote.model.VerifyResetOtpRequest
import com.pkm.sahabatgula.data.remote.model.VerifyResetOtpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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

    suspend fun verifyOtp(email: String, otp: String): Resource<VerifyOtpResponse> {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, otp))
            if(response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                val token = otpResponse.data.accessToken
                tokenManager.saveAccessToken(token)
                val myProfileResponse = apiService.getMyProfile("Bearer $token")
                if (myProfileResponse.isSuccessful) {
                    val body = myProfileResponse.body()
                    val myProfile = body?.data?.myProfile
                    if (myProfile != null) {
                        val profileEntity = myProfile.toProfileEntity()
                        profileDao.upsertProfile(profileEntity)
                        val isCompleted = isProfileCompleted(myProfile)
                        tokenManager.setProfileCompleted(isCompleted)
                    } else {
                        return Resource.Error("Gagal mengambil data profil")
                    }
                }
                Resource.Success(otpResponse)
            } else {
                Resource.Error("Gagal mengambil data profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val token = loginResponse.data.accessToken
                tokenManager.saveAccessToken(token)

                val myProfileResponse = apiService.getMyProfile("Bearer $token")
                if(myProfileResponse.isSuccessful) {
                    val body = myProfileResponse.body()
                    val myProfile = body?.data?.myProfile
                    if(myProfile != null){
                        val profileEntity = myProfile.toProfileEntity()
                        profileDao.upsertProfile(profileEntity)
                        val isCompleted = isProfileCompleted(myProfile)
                        tokenManager.setProfileCompleted(isCompleted)

                    } else {
                        return Resource.Error("Gagal mengambil data profil")
                    }
                }
                Resource.Success(loginResponse)

            } else {

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

    suspend fun verifyResetOtp(email:String, otp:String): Resource<VerifyResetOtpResponse> {
        return try {
            val request = VerifyResetOtpRequest(email, otp)
            val response = apiService.verifyResetOtp(request)
            if (response.isSuccessful && response.body() != null) {
                val resetOtpResponse = response.body()!!
                Resource.Success(resetOtpResponse)
            } else {
                Resource.Error("Gagal memverifikasi OTP: ${response.message()}")

            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun forgotPasswordInputEmail(email: String): Resource<ForgotPasswordResponse> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = apiService.forgotPasswordInputEmail(request)
            if(response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal mengirim email: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    suspend fun resetPassword(resetToken: String, newPassword: String): Resource<ResetPasswordResponse> {
        return try {
            val request = ResetPasswordRequest(resetToken, newPassword)
            val response = apiService.resetPassword(request)
            if (response.isSuccessful && response.body() != null) {
                val resetPasswordResponse = response.body()!!
                Resource.Success(resetPasswordResponse)
            } else {
                Resource.Error("Gagal mereset password: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }
    fun observeProfile(): Flow<ProfileEntity?> {
        return profileDao.observeProfile()
    }
    suspend fun getLocalProfile(): ProfileEntity? = withContext(Dispatchers.IO) {
        profileDao.getProfile()
    }

    suspend fun getMyProfile(token: String?): MyProfile {
        val myProfileResponse = apiService.getMyProfile("Bearer $token")
        if (myProfileResponse.isSuccessful) {
            val body = myProfileResponse.body()
            val myProfile = body?.data?.myProfile
            if (myProfile != null) {
                val profileEntity = myProfile.toProfileEntity()
                profileDao.upsertProfile(profileEntity)
            }
        }
        return myProfileResponse.body()?.data?.myProfile ?: throw IllegalStateException("Response body is null (getMyProfile)")
    }

}

fun MyProfile.toProfileEntity(): ProfileEntity {
    return ProfileEntity(
        id = this.userId,
        username = this.username,
        email = this.email,
        gender = null,
        age = null,
        height = this.height,
        weight = this.weight,
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

