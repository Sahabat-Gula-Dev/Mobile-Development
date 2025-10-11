package com.pkm.sahabatgula.ui.auth.forgotpassword.inputemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


interface ForgotPasswordEmailState {
    data object Idle : ForgotPasswordEmailState
    data object Loading : ForgotPasswordEmailState
    data class Success(val message: String?) : ForgotPasswordEmailState
    data class Error(val message: String) : ForgotPasswordEmailState
}

sealed class ForgotPasswordEmailEffect {
    data class ShowError(val message: String) : ForgotPasswordEmailEffect()
    data class ShowInfo(val message: String) : ForgotPasswordEmailEffect()
    data class NavigateToOtpVerification(val email: String) : ForgotPasswordEmailEffect()
}


@HiltViewModel
class ForgotPasswordEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordEmailState>(ForgotPasswordEmailState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEmailEffect>()
    val effect = _effect.asSharedFlow()

    fun requestOtp(email: String) {
        // Validasi email dulu
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch {
                _effect.emit(ForgotPasswordEmailEffect.ShowError("Email tidak valid"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = ForgotPasswordEmailState.Loading

            val result = authRepository.forgotPasswordInputEmail(email)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = ForgotPasswordEmailState.Success(result.data?.message)
                    _effect.emit(ForgotPasswordEmailEffect.NavigateToOtpVerification(email))
                }

                is Resource.Error -> {
                    _uiState.value = ForgotPasswordEmailState.Error(result.message ?: "Terjadi kesalahan")
                    _effect.emit(ForgotPasswordEmailEffect.ShowError(result.message ?: "Terjadi kesalahan"))
                }

                is Resource.Loading -> {
                    _uiState.value = ForgotPasswordEmailState.Loading
                }
            }
        }
    }
}
