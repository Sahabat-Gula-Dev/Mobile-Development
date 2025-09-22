package com.pkm.sahabatgula.data.local

import android.util.Log
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val profileDao: ProfileDao
) {
    suspend fun getCurrentUser(): ProfileEntity? {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
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

}