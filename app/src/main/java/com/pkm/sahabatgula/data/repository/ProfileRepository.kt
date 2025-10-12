package com.pkm.sahabatgula.data.repository

import android.util.Log
import com.google.gson.Gson
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.SessionManager
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.ErrorResponse
import com.pkm.sahabatgula.data.remote.model.ProfileData
import com.pkm.sahabatgula.data.remote.model.SetupProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val profileDao: ProfileDao,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) {
    suspend fun setupProfile(profileData: ProfileData): Resource<SetupProfileResponse> {
        val currentUser = sessionManager.getCurrentUser()
            ?: return Resource.Error("Sesi pengguna tidak valid. Silakan login ulang.")

        return try {
            val token = tokenManager.getAccessToken()
                ?: return Resource.Error("Token tidak ditemukan, silakan login kembali")

            val response = apiService.setupProfile("Bearer ${token}", profileData)
            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                val profileEntity = profileData.toProfileEntity(
                    userId = currentUser.id,
                    username = currentUser.username,
                    response = profileResponse,
                    email = currentUser.email)

                profileDao.upsertProfile(profileEntity)
                tokenManager.setProfileCompleted(true)
                Resource.Success(profileResponse)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody.isNullOrEmpty()) {
                    "Terjadi Kesalahan: ${response.code()}"
                } else {
                    try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Terjadi Kesalahan"
                    } catch (e: Exception) {
                        "Terjadi Kesalahan: ${response.code()} - ${e.message}"
                    }
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi Kesalahan")
        }
    }

    suspend fun fetchMyProfileAndCache() {
        val token = tokenManager.getAccessToken() ?: return
        val response = apiService.getMyProfile("Bearer $token")
        if (response.isSuccessful) {
            val myProfile = response.body()?.data?.myProfile
            if (myProfile != null) {
                profileDao.upsertProfile(myProfile.toProfileEntity())
            }
        } else {
            Log.e("ProfileRepository", "Gagal fetch profil ulang: ${response.message()}")
        }
    }

}

fun ProfileData.toProfileEntity(
    userId: String,
    username: String?,
    email: String?,
    response: SetupProfileResponse
): ProfileEntity {
    return ProfileEntity (
        id = userId,
        username = username,
        email = email?:"",
        gender = this.gender,
        age = this.age,
        height = this.height,
        weight = this.weight,
        waist_circumference = this.waistCircumference,
        blood_pressure = this.bloodPressure,
        blood_sugar = this.bloodSugar,
        eat_vegetables = this.eatVegetables,
        diabetes_family = this.diabetesFamily,
        activity_level = this.activityLevel,

        risk_index = response.setupProfileData.riskIndex,
        bmi_score = response.setupProfileData.bmi,
        max_calories = response.setupProfileData.maxCalories,
        max_carbs = response.setupProfileData.carbs,
        max_protein = response.setupProfileData.protein,
        max_fat = response.setupProfileData.fat,
        max_sugar = response.setupProfileData.sugar,

        max_natrium = null,
        max_fiber = null,
        max_potassium = null
    )
}