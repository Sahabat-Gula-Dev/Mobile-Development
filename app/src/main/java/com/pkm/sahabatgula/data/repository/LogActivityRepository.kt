package com.pkm.sahabatgula.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.SummaryDao
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.ActivitiesDataItem
import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.data.remote.model.LogActivityData
import com.pkm.sahabatgula.data.remote.model.LogActivityItemRequest
import com.pkm.sahabatgula.data.remote.model.LogActivityRequest
import com.pkm.sahabatgula.ui.home.dailyactivity.logactivity.ActivityPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class LogActivityRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val summaryDao: SummaryDao
) {

    private suspend fun updateLocalSummary(newTotals: LogActivityData) {
        val today = DateConverter.getTodayLocalFormatted()

        val currentSummary = summaryDao.getSummaryByDate("DAILY", today).firstOrNull()

        val updatedSummary = currentSummary?.copy(
            burned = (currentSummary.burned ?: 0) + newTotals.totalBurned
        )?:
        SummaryEntity(
            date = today,
            type = "DAILY",
            calories = 0.0,
            carbs = 0.0,
            protein = 0.0,
            fat = 0.0,
            sugar = 0.0,
            sodium = 0.0,
            fiber = 0.0,
            potassium = 0.0,
            burned = newTotals.totalBurned,
            steps = 0,
            water = 0
        )
        summaryDao.upsertAll(listOf(updatedSummary))
    }

    suspend fun logActivity(items: List<LogActivityItemRequest>): Resource<Unit> {
        val requestBody = LogActivityRequest(activities = items)

        return try {
            val token = tokenManager.getAccessToken()
                ?: return Resource.Error("Token tidak ditemukan, silakan login kembali")

            val response = apiService.logActivity("Bearer $token", requestBody)

            if (response.isSuccessful && response.body() != null) {
                val newTotals = response.body()!!.data
                updateLocalSummary(newTotals)
                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal menyimpan aktivitas: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    fun getActivityPaginated(query:String?, categoryId:Int?): Flow<PagingData<ActivitiesDataItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ActivityPagingSource(apiService, query, categoryId)
            }
        ).flow
    }

    suspend fun getActivityCategories(): Resource<List<ActivityCategories>> {
        return try {
            val response = apiService.getActivityCategories()
            Log.e("ActivityCategories", "code=${response.code()}, msg=${response.message()}, body=${response.errorBody()?.string()}")
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                Resource.Success(data)
            } else {
                Resource.Error("Gagal mengambil kategori aktivitas: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }
}