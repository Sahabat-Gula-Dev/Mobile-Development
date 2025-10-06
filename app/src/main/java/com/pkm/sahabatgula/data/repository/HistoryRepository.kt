package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.data.remote.api.ApiService
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getUserHistory(token: String) = api.getUserHistory("Bearer $token")
}