package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.DailySummaryDao
import com.pkm.sahabatgula.data.local.room.DailySummaryEntity
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.DailySummaryResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val profileDao: ProfileDao,
    private val dailySummaryDao: DailySummaryDao
    ) {

    suspend fun getDailySummary(): Resource<DailySummaryResponse> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Resource.Error("Token tidak ditemukan, silakan login kembali")
            }
            val response = apiService.getDailySummary("Bearer $token")
            if (response.isSuccessful && response.body() != null){
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Gagal memuat ringkasan harian: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Terjadi kesalahan jaringan")
        }
    }

    suspend fun getProfile(): ProfileEntity {
        return profileDao.getProfile()

    }
    fun observeDailySummary(date: String): Flow<DailySummaryEntity?> {
        return dailySummaryDao.getSummaryByDate(date)
    }

    suspend fun refreshDailySummary() {
        val token = tokenManager.getAccessToken() ?: return
        val response = apiService.getDailySummary("Bearer $token")
        if (response.isSuccessful) {
            response.body()?.let {
                dailySummaryDao.upsert(it.toEntity()) // simpan ke Room
            }
        }
    }
}

private fun DailySummaryResponse.toEntity(): DailySummaryEntity {
    return DailySummaryEntity(
        date = this.data?.summary?.date ?: "",
        calories = this.data?.summary?.nutrients?.calories?.toInt() ?: 0,
        carbs = this.data?.summary?.nutrients?.carbs ?: 0.0,
        protein = this.data?.summary?.nutrients?.protein ?: 0.0,
        fat = this.data?.summary?.nutrients?.fat ?: 0.0,
        sugar = this.data?.summary?.nutrients?.sugar ?: 0.0,
        sodium = this.data?.summary?.nutrients?.sodium ?: 0.0,
        fiber = this.data?.summary?.nutrients?.fiber ?: 0.0,
        potassium = this.data?.summary?.nutrients?.potassium ?: 0.0,
        burned = this.data?.summary?.activities?.burned ?: 0,
        steps = this.data?.summary?.steps ?: 0,
        water = this.data?.summary?.water ?: 0
    )
}
