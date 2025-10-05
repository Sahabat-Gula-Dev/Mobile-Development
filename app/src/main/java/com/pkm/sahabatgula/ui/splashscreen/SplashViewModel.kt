package com.pkm.sahabatgula.ui.splashscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.pkm.sahabatgula.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.remote.api.ApiService
import kotlinx.coroutines.launch
import java.io.IOException

enum class SplashDestination {
    AUTH_FLOW,
    INPUT_DATA_FLOW,
    HOME_FLOW,
    LOADING
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
): ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination= _destination.asStateFlow()



    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1200)

                if (!sessionManager.isLoggedIn()) {
                    _destination.value = SplashDestination.AUTH_FLOW
                    return@launch
                }

                val profile = sessionManager.getOrFetchProfile(apiService)
                if (profile == null) {
                    _destination.value = SplashDestination.AUTH_FLOW
                    return@launch
                }

                if (sessionManager.isProfileCompleted()) {
                    _destination.value = SplashDestination.HOME_FLOW
                } else {
                    _destination.value = SplashDestination.INPUT_DATA_FLOW
                }

            } catch (e: IOException) {
                Log.w("SplashViewModel", "Network error. Relying on local session flags.")
                Log.e("SplashViewModel", "Network error. Relying on local session flags.")

                if (sessionManager.isProfileCompleted()) {
                    _destination.value = SplashDestination.HOME_FLOW
                } else {
                    _destination.value = SplashDestination.INPUT_DATA_FLOW
                }

            } catch (e: Exception) {
                Log.e("SplashViewModel", "Unexpected error. Logging out for safety.", e)
                sessionManager.clearSession()
                _destination.value = SplashDestination.AUTH_FLOW
            }
        }
    }

}