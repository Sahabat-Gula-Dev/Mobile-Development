package com.pkm.sahabatgula.ui.auth.forgotpassword.newpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(private val authRepository: AuthRepository): ViewModel(){

    private val _uiState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val uiState: StateFlow<ResetPasswordState> = _uiState

    private val _effect = MutableSharedFlow<ResetPasswordEffect>()
    val uiEffect: SharedFlow<ResetPasswordEffect> = _effect

    fun resetPassword(resetToken: String, newPassword: String, confirmPassword: String) {
        if(newPassword.isBlank() || confirmPassword.isBlank()) {
            viewModelScope.launch {
                _effect.emit(ResetPasswordEffect.ShowToast("Password tidak boleh kosong"))
                return@launch
            }
        }
        if (newPassword != confirmPassword) {
            viewModelScope.launch {
                _effect.emit(ResetPasswordEffect.ShowToast("Konfirmasi password tidak sesuai"))
                return@launch
            }
        }
        viewModelScope.launch {
            _uiState.value = ResetPasswordState.Loading
            when(val result = authRepository.resetPassword(resetToken, newPassword)) {
                is Resource.Success -> {
                    _uiState.value = ResetPasswordState.Success
                    _effect.emit(ResetPasswordEffect.ShowToast(result.data!!.message))
                    _effect.emit(ResetPasswordEffect.NavigateToLogin)
                }
                is Resource.Error -> {
                    _uiState.value = ResetPasswordState.Idle
                    _effect.emit(ResetPasswordEffect.ShowToast(result.message))
                }
                is Resource.Loading -> {

                }


            }
        }
    }

}
