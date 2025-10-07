package com.pkm.sahabatgula.ui.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.remote.model.GoogleAuthRequest
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


sealed interface LoginEffect {
    data class ShowToast(val message: String): LoginEffect
    object NavigateToHome : LoginEffect
    object NavigateToWelcome : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {

    private val _loginState = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _effect : Channel<LoginEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginViewState.Loading
            val resource = repository.login(email, password)
            when (resource) {
                is Resource.Success -> {
                    _loginState.value = LoginViewState.Success(resource.data)


                    val profile = withContext(Dispatchers.IO) {
                        repository.getLocalProfile()
                    }

                    if (profile == null || profile.height == null || profile.height == 0) {
                        Log.d("LoginViewModel", "Profile belum lengkap, age=${profile?.age}")
                        _effect.send(LoginEffect.NavigateToWelcome)
                    } else {
                        Log.d("LoginViewModel", "Profile lengkap, age=${profile.age}")
                        _effect.send(LoginEffect.NavigateToHome)
                    }
                }
                is Resource.Error -> {
                    _loginState.value = LoginViewState.Error(resource.message)
                }
                is Resource.Loading<*> -> {
                    Log.d("LoginViewModel", "login: tunggu dulu")
                }
            }
        }
    }
}


//    fun signInWithGoogle(idToken: String) {
//        viewModelScope.launch {
//            _loginState.value = LoginViewState.Loading
//
//            repository.googleAuth(GoogleAuthRequest(idToken))
//                .onSuccess { response ->
//                    val accessToken = response.data.accessToken
//                    if (accessToken.isNullOrEmpty()) {
//                        _loginState.value = Error("Token tidak valid dari server") as LoginViewState
//                        _effect.send(LoginEffect.ShowToast("Token tidak valid dari server"))
//                        return@onSuccess
//                    }
//
//                    tokenManager.saveAccessToken(accessToken)
//
//                    if (response.isNewUser) {
//                        // user baru → ke screen setup profil
//                        _effect.send(LoginEffect.ShowToast("Akun baru, silakan lengkapi data profil"))
//                        _effect.send(LoginEffect.NavigateToWelcome) // bikin effect baru
//                    } else {
//                        // user lama → langsung ke home
//                        _effect.send(LoginEffect.NavigateToHome)
//                    }
//
//                    _loginState.value = LoginViewState.OnSuccess(response.data)
//                }
//                .onFailure { e ->
//                    _loginState.value = Error(e.message ?: "Gagal login dengan Google") as LoginViewState
//                    _effect.send(LoginEffect.ShowToast(e.message ?: "Gagal login dengan Google"))
//                }
//        }
//    }



