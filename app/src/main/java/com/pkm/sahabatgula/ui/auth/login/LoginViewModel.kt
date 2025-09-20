package com.pkm.sahabatgula.ui.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.AuthRepository
import com.pkm.sahabatgula.ui.auth.login.LoginViewState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


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
            _loginState.value = Loading
            val resource = repository.login(email, password)
            when (resource) {
                is Resource.Success -> {
                    _loginState.value = Success(resource.data)
                    _effect.send(LoginEffect.NavigateToHome)
                }
                is Resource.Error -> {
                    _loginState.value = Error(resource.message)
                }

                is Resource.Loading<*> -> {
                    Log.d("LoginViewModel", "login: tunggu dulu")
                }
            }
        }
    }
}