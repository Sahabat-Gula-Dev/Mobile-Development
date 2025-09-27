package com.pkm.sahabatgula.data.repository

import android.util.Log
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.local.room.SummaryDao
import com.pkm.sahabatgula.data.local.room.SummaryEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.SummaryData
import com.pkm.sahabatgula.core.utils.DateConverter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val profileDao: ProfileDao,
    private val summaryDao: SummaryDao
) {
    fun observeDailySummary(date: String): Flow<SummaryEntity?> {
        Log.d("HomeRepository", "Observing DAILY summary untuk tanggal: $date")
        return summaryDao.getSummaryByDate("DAILY", date)
    }

    fun observeWeeklySummary(): Flow<List<SummaryEntity>> {
        Log.d("HomeRepository", "Observing WEEKLY summary data from DB")
        return summaryDao.getAllWeeklySummary()
    }

    fun observeMonthlySummary(): Flow<List<SummaryEntity>> {
        Log.d("HomeRepository", "Observing MONTHLY summary data from DB")
        return summaryDao.getAllMonthlySummary()
    }

    fun observeProfile(): Flow<ProfileEntity?> {
        return profileDao.observeProfile()
    }

    suspend fun refreshDailySummary(): Resource<Unit> {
        return try {
            val token = tokenManager.getAccessToken()
                ?: return Resource.Error("Token tidak ditemukan")
            val response = apiService.getSummary("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DATA_CHECK", "1. Raw API Response Body: $body")

                val serverTimestamp = body.data.daily.date
                if (serverTimestamp != null) {
                    val localDate = DateConverter.convertServerDateToLocal(serverTimestamp)
                    Log.d("DATA_CHECK", "2. Date Conversion: $serverTimestamp -> $localDate")

                    val entities = body.data.toEntity()
                    summaryDao.upsertAll(entities)
                    Log.d("DB_CHECK", "Summary table: ${summaryDao.getAll()}")
                }

                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal update data: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Kesalahan jaringan: ${e.message}")
        }
    }

}



fun SummaryData.toEntity(): List<SummaryEntity> {
    val entities = mutableListOf<SummaryEntity>()

    daily.let { d ->
        entities.add(
            SummaryEntity(
                type = "DAILY",
                date = DateConverter.convertServerDateToLocal(d.date),
                calories = d.nutrients.calories,
                carbs = d.nutrients.carbs,
                protein = d.nutrients.protein,
                fat = d.nutrients.fat,
                sugar = d.nutrients.sugar,
                sodium = d.nutrients.sodium,
                fiber = d.nutrients.fiber,
                potassium = d.nutrients.potassium,
                burned = d.activities.burned,
                steps = d.steps,
                water = d.water
            )
        )
    }

    weekly?.forEach { w ->
        entities.add(
            SummaryEntity(
                type = "WEEKLY",
                date = w.date,
                calories = w.nutrients.calories,
                carbs = w.nutrients.carbs,
                protein = w.nutrients.protein,
                fat = w.nutrients.fat,
                sugar = w.nutrients.sugar,
                sodium = w.nutrients.sodium,
                fiber = w.nutrients.fiber,
                potassium = w.nutrients.potassium,
                burned = w.activities.burned,
                steps = w.steps,
                water = w.water
            )
        )
    }

    monthly?.forEach { m ->
        entities.add(
            SummaryEntity(
                type = "MONTHLY",
                date = m.date,
                calories = m.nutrients.calories,
                carbs = m.nutrients.carbs,
                protein = m.nutrients.protein,
                fat = m.nutrients.fat,
                sugar = m.nutrients.sugar,
                sodium = m.nutrients.sodium,
                fiber = m.nutrients.fiber,
                potassium = m.nutrients.potassium,
                burned = m.activities.burned,
                steps = m.steps,
                water = m.water
            )
        )
    }

    return entities
}

