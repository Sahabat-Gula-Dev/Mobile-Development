package com.pkm.sahabatgula.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.DateConverter
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.SummaryDao
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.data.remote.model.Food
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
import com.pkm.sahabatgula.data.remote.model.LogFoodRequest
import com.pkm.sahabatgula.data.remote.model.Totals
import com.pkm.sahabatgula.ui.explore.article.ArticlePagingSource
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.FoodPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogFoodRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val summaryDao: SummaryDao
) {

    suspend fun logFood(items: List<FoodItemRequest>): Resource<Unit> {
        val requestBody = LogFoodRequest(foods = items)
        try {
            val token = tokenManager.getAccessToken() ?: return Resource.Error("Token tidak ditemukan")
            val response = apiService.logFood("Bearer $token", requestBody)

            if (response.isSuccessful && response.body() != null) {
                val newTotals = response.body()!!.data.totals
                updateLocalSummary(newTotals)

                return Resource.Success(Unit)
            } else {
                return Resource.Error("Gagal mencatat makanan: ${response.message()}")
            }
        } catch (e: Exception) {
            return Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    fun getFoodPaginated(query:String?, categoryId:Int?): Flow<PagingData<FoodItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FoodPagingSource(apiService, query, categoryId)
            }
        ).flow
    }

    fun getFoodPagingData(query: String?, categoryId: Int?): Flow<PagingData<FoodItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FoodPagingSource(apiService, query, categoryId)
            }
        ).flow
    }


    suspend fun getFoodCategories(): Resource<List<FoodCategories>> {
        return try {
            val response = apiService.getFoodCategories()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                Resource.Success(data)
            } else {
                Resource.Error("Gagal mengambil kategori makanan: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }

    suspend fun searchFoods(query: String?, categoryId: Int?): List<FoodItem> {
        return try {
            val response = apiService.getFoods(
                page = 1,
                limit = 5, // cukup ambil sedikit untuk cek ada/tidak
                query = query,
                categoryId = categoryId
            )

            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    private suspend fun updateLocalSummary(newTotals: Totals) {
        val today = DateConverter.getTodayLocalFormatted()

        val currentSummary = summaryDao.getSummaryByDate("DAILY", today).firstOrNull()

        if (currentSummary != null) {
            val updatedSummary = currentSummary.copy(
                calories = currentSummary.calories?.plus(newTotals.calories),
                carbs = currentSummary.carbs?.plus(newTotals.carbs),
                protein = currentSummary.protein?.plus(newTotals.protein),
                fat = currentSummary.fat?.plus(newTotals.fat),
                sugar = currentSummary.sugar?.plus(newTotals.sugar),
                sodium = currentSummary.sodium?.plus(newTotals.sodium),
                fiber = currentSummary.fiber?.plus(newTotals.fiber),
                potassium = currentSummary.potassium?.plus(newTotals.potassium)
            )
            summaryDao.upsertAll(listOf(updatedSummary))

        } else {
            val newSummary = SummaryEntity(
                date = today,
                type = "DAILY",
                calories = newTotals.calories,
                carbs = newTotals.carbs,
                protein = newTotals.protein,
                fat = newTotals.fat,
                sugar = newTotals.sugar,
                sodium = newTotals.sodium,
                fiber = newTotals.fiber,
                potassium = newTotals.potassium,
                burned = 0,
                steps = 0,
                water = 0
            )
            summaryDao.upsertAll(listOf(newSummary))
        }
    }
}