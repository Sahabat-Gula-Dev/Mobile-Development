package com.pkm.sahabatgula.ui.auth.forgotpassword.inputemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordEmailViewModel @Inject constructor(private val authRepository: AuthRepository): ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordEmailState>(ForgotPasswordEmailState.Idle)
    val uiState: StateFlow<ForgotPasswordEmailState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEmailEffect>()
    val effect = _effect.asSharedFlow()

    fun requestOtp(email: String){
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch {
                _effect.emit(ForgotPasswordEmailEffect.ShowToast("Email tidak valid"))
                return@launch
            }
        }
        viewModelScope.launch {
            _uiState.value = ForgotPasswordEmailState.Loading
            val result = authRepository.forgotPasswordInputEmail(email)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = ForgotPasswordEmailState.Success(result.data?.message)
                    _effect.emit(ForgotPasswordEmailEffect.NavigateToOtpVerification(email))
                }
                is Resource.Error-> {
                    _uiState.value = ForgotPasswordEmailState.Idle
                    _uiState.value = ForgotPasswordEmailState.Error(result.message ?: "Terjadi Kesalahan")
                    _effect.emit(ForgotPasswordEmailEffect.ShowToast(result.message ?: "Terjadi Kesalahan"))
                }
                is Resource.Loading -> {}
            }
        }

    }
}