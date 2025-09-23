package com.pkm.sahabatgula.data.repository

import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
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
    private val profileDao: ProfileDao
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
}