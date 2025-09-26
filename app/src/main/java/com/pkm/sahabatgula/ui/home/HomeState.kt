package com.pkm.sahabatgula.ui.home

import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.DailySummaryResponse
import com.pkm.sahabatgula.data.remote.model.SummaryResponse

sealed interface HomeState {
    data object  Loading: HomeState
    data class Error(val message: String): HomeState
    data class Success(
        val profile: ProfileEntity,
        val summary: SummaryResponse
    ): HomeState
}

sealed interface HomeEffect {
    data class ShowToast(val message: String): HomeEffect
}