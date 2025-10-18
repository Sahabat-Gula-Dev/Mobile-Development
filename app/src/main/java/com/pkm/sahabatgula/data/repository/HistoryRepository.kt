package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getUserHistory(token: String): List<HistoryItem>? {
        val response = api.getUserHistory("Bearer $token")
        if (response.isSuccessful.not()) {
            throw Exception("Gagal memuat riwayat: ${response.message()}")
        }
        return response.body()?.data?.map { item ->
            item.copy(date = DateConverter.convertServerDateToLocal(item.date))
        }
    }
}
