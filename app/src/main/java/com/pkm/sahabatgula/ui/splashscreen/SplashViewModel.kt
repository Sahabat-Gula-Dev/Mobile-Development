package com.pkm.sahabatgula.ui.splashscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.pkm.sahabatgula.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


// Enum untuk merepresentasikan tujuan navigasi
enum class SplashDestination {
    AUTH_FLOW,          // Arahkan ke Login/Register
    INPUT_DATA_FLOW,    // Arahkan ke pengisian profil
    HOME_FLOW,          // Arahkan ke halaman utama
    LOADING             // Masih dalam proses pengecekan
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
): ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination= _destination.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)

            val user = sessionManager.getCurrentUser()
            val token = sessionManager.isProfileCompleted()

            Log.d("DARI SPLASH", "user dari db: $user")
            Log.d("DARI SPLASH", "token dari db: $token")
            Log.d("DARI SPLASH", "profile completed flag: ${sessionManager.isProfileCompleted()}")

            if (user == null) {
                _destination.value = SplashDestination.AUTH_FLOW
            } else {
                if (!sessionManager.isProfileCompleted()) {
                    _destination.value = SplashDestination.INPUT_DATA_FLOW
                } else {
                    _destination.value = SplashDestination.HOME_FLOW
                }
            }
        }
    }
}