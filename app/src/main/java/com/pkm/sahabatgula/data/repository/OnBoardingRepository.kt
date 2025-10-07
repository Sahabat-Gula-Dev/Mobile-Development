package com.pkm.sahabatgula.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_IS_FIRST_TIME = "is_first_time"
    }

    fun isFirstTime(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_TIME, true)
    }

    fun setFirstTime(isFirstTime: Boolean) {
        prefs.edit { putBoolean(KEY_IS_FIRST_TIME, isFirstTime) }
    }
}