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

                Log.d("PROFILE_SETUP", "Profile disimpan ke DB: $profileEntity")
                Log.d("PROFILE_SETUP", "ProfileCompleted Flag diset ke true")

                Resource.Success(profileResponse)

//                val resp = response.body()!!
//                val old = profileDao.getProfile()?: currentUser.toProfileEntityFallback()
//                val merged = mergeProfile(old, profileData, resp)
//                profileDao.upsertProfile(merged)
//                tokenManager.setProfileCompleted(true)
//                Log.d("PROFILE_SETUP", "Profile disimpan ke DB: $merged")
//                Log.d("PROFILE_SETUP", "ProfileCompleted Flag diset ke true")
//                Resource.Success(resp)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody.isNullOrEmpty()) {
                    "Terjadi Kesalahan: ${response.code()}"
                } else {
                    try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.message ?: "Terjadi Kesalahan"
                    } catch (e: Exception) {
                        "Terjadi Kesalahan"
                    }
                }
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi Kesalahan")
        }
    }
}

//private fun ProfileEntity.toProfileEntityFallback(): ProfileEntity = this
//
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

        // Mengambil setupProfileData hasil kalkulasi dari response API
        risk_index = response.setupProfileData.riskIndex,
        bmi_score = response.setupProfileData.bmi,
        max_calories = response.setupProfileData.maxCalories,
        max_carbs = response.setupProfileData.carbs,
        max_protein = response.setupProfileData.protein,
        max_fat = response.setupProfileData.fat,
        max_sugar = response.setupProfileData.sugar,

        // Kolom ini tidak ada di response, jadi kita beri null atau nilai default
        max_natrium = null,
        max_fiber = null,
        max_potassium = null
    )
}
//
//private fun <T> prefer(server: T?, form: T?, old: T?): T? = server ?: form ?: old
//
//fun mergeProfile(
//    old: ProfileEntity,
//    form: ProfileData,
//    resp: SetupProfileResponse?
//): ProfileEntity {
//    return old.copy(
//        gender =          prefer(null,             form.gender,            old.gender),
//        age =             prefer(null,             form.age,               old.age),
//        height =          prefer(null,             form.height,            old.height),
//        weight =          prefer(null,             form.weight,            old.weight),
//        waist_circumference = prefer(null,         form.waistCircumference, old.waist_circumference),
//        blood_pressure =  prefer(null,             form.bloodPressure,     old.blood_pressure),
//        blood_sugar =     prefer(null,             form.bloodSugar,        old.blood_sugar),
//        eat_vegetables =  prefer(null,             form.eatVegetables,     old.eat_vegetables),
//        diabetes_family = prefer(null,             form.diabetesFamily,    old.diabetes_family),
//        activity_level =  prefer(null,             form.activityLevel,     old.activity_level),
//
//        // bagian kalkulasi biasanya datang dari server setelah setup
//        risk_index =      prefer(resp?.setupProfileData?.riskIndex,  null,                   old.risk_index),
//        bmi_score =       prefer(resp?.setupProfileData?.bmi,   null,                   old.bmi_score),
//        max_calories =    prefer(resp?.setupProfileData?.maxCalories,null,                   old.max_calories),
//        max_carbs =                     old.max_carbs,
//        max_protein =     old.max_protein,
//        max_fat =         old.max_fat,
//        max_sugar =       old.max_sugar,
//        max_natrium =     old.max_natrium,
//        max_fiber =       old.max_fiber,
//        max_potassium =   old.max_potassium
//    )
//}

