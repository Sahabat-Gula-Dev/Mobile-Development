package com.pkm.sahabatgula.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import android.util.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_app_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("TokenManager", "EncryptedSharedPreferences corrupted, resetting...", e)
        // Jika gagal decrypt, hapus shared pref & buat ulang
        context.deleteSharedPreferences("auth_app_prefs")

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_app_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
        private const val PROFILE_COMPLETED_KEY = "PROFILE_COMPLETED"
    }

    // === TOKEN ===
    fun saveAccessToken(token: String?) {
        sharedPreferences.edit { putString(ACCESS_TOKEN_KEY, token) }
    }

    fun getAccessToken(): String? {
        return try {
            sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
        } catch (e: Exception) {
            Log.e("TokenManager", "Failed to decrypt access token", e)
            clearAccessToken()
            null
        }
    }

    fun clearAccessToken() {
        sharedPreferences.edit { remove(ACCESS_TOKEN_KEY) }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun isAccessTokenExpired(): Boolean {
        val token = getAccessToken() ?: return true
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val exp = JSONObject(payload).getLong("exp")
            val now = System.currentTimeMillis() / 1000
            now >= exp
        } catch (e: Exception) {
            Log.w("TokenManager", "Failed to parse token exp", e)
            true
        }
    }

    fun setProfileCompleted(completed: Boolean) {
        sharedPreferences.edit { putBoolean(PROFILE_COMPLETED_KEY, completed) }
    }

    fun isProfileCompleted(): Boolean {
        return try {
            sharedPreferences.getBoolean(PROFILE_COMPLETED_KEY, false)
        } catch (e: Exception) {
            Log.e("TokenManager", "Failed to decrypt profile flag", e)
            false
        }
    }

    fun clearProfileCompleted() {
        sharedPreferences.edit { remove(PROFILE_COMPLETED_KEY) }
    }
}