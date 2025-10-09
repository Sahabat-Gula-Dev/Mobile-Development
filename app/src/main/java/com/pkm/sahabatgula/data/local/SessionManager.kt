package com.pkm.sahabatgula.data.local

import android.util.Log
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.local.room.SummaryDao
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.repository.toProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileDao: ProfileDao,
    private val summaryDao: SummaryDao,
) {
    suspend fun getCurrentUser(): ProfileEntity? {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty() || tokenManager.isAccessTokenExpired()) {
            clearSession()
            return null
        }
        return profileDao.getProfile()
    }


    fun setProfileCompleted(completed: Boolean) {
        Log.d("PROFILE_SETUP", " SET - ProfileCompleted Flag diset ke $completed")
        tokenManager.setProfileCompleted(completed)
    }

    fun isProfileCompleted(): Boolean {
        val result = tokenManager.isProfileCompleted()
        Log.d("PROFILE_SETUP", "GET - ProfileCompleted Flag diambil: $result")
        return result
    }

    suspend fun isLoggedIn(): Boolean {
        val token = tokenManager.getAccessToken()
        return !token.isNullOrEmpty() && !tokenManager.isAccessTokenExpired()
    }


    suspend fun getOrFetchProfile(apiService: ApiService): ProfileEntity? {
        val token = tokenManager.getAccessToken() ?: return null
        if (tokenManager.isAccessTokenExpired()) {
            clearSession()
            return null
        }

        val localProfile = profileDao.getProfile()
        if (localProfile != null) return localProfile

        val response = apiService.getMyProfile("Bearer $token")
        if (response.isSuccessful && response.body()?.data?.myProfile != null) {
            val profile = response.body()!!.data.myProfile.toProfileEntity()
            profileDao.upsertProfile(profile)
            return profile
        } else {
            // Bisa karena token expired di server
            clearSession()
            return null
        }
    }
    
    suspend fun clearSession() {
        tokenManager.clearAccessToken()
        tokenManager.clearProfileCompleted()
        profileDao.clearAll()
        summaryDao.clearAllSumary()
        Log.d("SessionManager", "Session cleared")
    }
}