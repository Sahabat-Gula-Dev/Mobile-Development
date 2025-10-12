package com.pkm.sahabatgula.ui.state

import com.pkm.sahabatgula.R

sealed class GlobalUiState {
    data class Loading(val message: String? = null, val imageRes: Int? = null) : GlobalUiState()
    data class Success(val title: String, val message: String? = null, val imageRes: Int? = null, val calorieValue: Int? = null, val actionText: String? = null) : GlobalUiState()
    data class Error(val title: String, val message: String, val imageRes: Int? = null,  val actionText: String? = null) : GlobalUiState()
    annotation class Companion
    object None : GlobalUiState()
}
