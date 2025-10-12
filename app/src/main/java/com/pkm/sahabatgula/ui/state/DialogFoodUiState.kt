package com.pkm.sahabatgula.ui.state

sealed class DialogFoodUiState {
    data class Loading(val message: String? = null, val imageRes: Int? = null) : DialogFoodUiState()
    data class Success(
        val title: String,
        val message: String? = null,
        val imageRes: Int? = null,
        val calorieValue: Int? = null,
        val carbo: Int? = null,
        val protein: Int? = null,
        val fat: Int? = null,
        val sugar: Double? = null,
        val sodium: Double? = null,
        val fiber: Double? = null,
        val kalium: Double? = null,
    ) : DialogFoodUiState()
    data class Error(val title: String, val message: String, val imageRes: Int? = null,  val actionText: String? = null) : DialogFoodUiState()
    annotation class Companion
    object None : DialogFoodUiState()
}