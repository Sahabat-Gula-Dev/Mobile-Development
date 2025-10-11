package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword

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

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ResetPasswordEffect>(replay = 1)
    val uiEffect = _effect.asSharedFlow()

    fun resetPassword(resetToken: String, newPassword: String, confirmPassword: String) {
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = ResetPasswordState.Error("Password tidak boleh kosong")
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.value = ResetPasswordState.Error("Konfirmasi password tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResetPasswordState.Loading
            when (val result = authRepository.resetPassword(resetToken, newPassword)) {
                is Resource.Success -> {
                    _uiState.value = ResetPasswordState.Success(result.data?.message ?: "Password berhasil diubah")
                    _effect.emit(ResetPasswordEffect.NavigateToLogin)
                }
                is Resource.Error -> {
                    _uiState.value = ResetPasswordState.Error(result.message ?: "Terjadi kesalahan")
                }
                is Resource.Loading -> {
                    _uiState.value = ResetPasswordState.Loading
                }
            }
        }
    }
}
