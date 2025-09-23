package com.pkm.sahabatgula.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_app_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
        private const val PROFILE_COMPLETED_KEY = "PROFILE_COMPLETED"
    }

    fun saveAccessToken(token: String?) {
        sharedPreferences.edit { putString(ACCESS_TOKEN_KEY, token) }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun clearAccessToken() {
        sharedPreferences.edit { remove(ACCESS_TOKEN_KEY) }
    }

    // === PROFILE FLAG ===
    fun setProfileCompleted(completed: Boolean) {
        sharedPreferences.edit { putBoolean(PROFILE_COMPLETED_KEY, completed) }
    }

    fun isProfileCompleted(): Boolean {
        return sharedPreferences.getBoolean(PROFILE_COMPLETED_KEY, false)
    }

    fun clearProfileCompleted() {
        sharedPreferences.edit { remove(PROFILE_COMPLETED_KEY) }
    }
}