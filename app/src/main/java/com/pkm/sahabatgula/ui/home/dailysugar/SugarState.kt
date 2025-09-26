package com.pkm.sahabatgula.ui.home.dailysugar

sealed class SugarState {
    object Loading: SugarState()
    data class Success(
        val currentSugar: Double,
        val maxSugar: Double
    ): SugarState() {
        val remainingSugar: Double get() = maxSugar - currentSugar
    }
}