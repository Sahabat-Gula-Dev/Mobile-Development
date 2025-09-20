package com.pkm.sahabatgula.ui.splashscreen

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
            if(user == null) {
                _destination.value = SplashDestination.AUTH_FLOW
            } else {
                if(user.gender.isNullOrEmpty()) {
                    _destination.value = SplashDestination.INPUT_DATA_FLOW // nanti ganti jadi welcome screen (krn ini masih debug)
                } else {
                    _destination.value = SplashDestination.HOME_FLOW
                }
            }
        }
    }
}