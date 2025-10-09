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
import com.pkm.sahabatgula.data.remote.model.LogWaterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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

    // get profile entity
    fun observeProfileEntity(): Flow<ProfileEntity?> {
        return profileDao.observeProfile()
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

    suspend fun updateWaterIntake(increment: Int): Resource<Unit> {
        val today = DateConverter.getTodayLocalFormatted()
        return try {
            // --- 1. Optimistic Update ke Room ---
            val currentSummary = summaryDao.getSummaryByDate("DAILY", today).firstOrNull()
            val updatedSummary = currentSummary?.copy(water = (currentSummary.water ?: 0) + increment)
                ?: // kalau belum ada data hari ini, bikin baru
                SummaryEntity(
                    type = "DAILY",
                    date = today,
                    water = increment,
                    calories = 0.0,
                    carbs = 0.0,
                    protein = 0.0,
                    fat = 0.0,
                    sugar = 0.0,
                    sodium = 0.0,
                    fiber = 0.0,
                    potassium = 0.0,
                    burned = 0,
                    steps = 0
                )
            summaryDao.upsertAll(listOf(updatedSummary))
            Log.d("WaterUpdate", "Optimistic update Room: +$increment ml")

            val token = tokenManager.getAccessToken() ?: return Resource.Error("Token tidak valid")
            val requestBody = LogWaterRequest(increment) // kirim jumlah increment, bukan total
            val response = apiService.logWater("Bearer $token", requestBody)

            if (response.isSuccessful) {
                Log.d("WaterUpdate", "API update successful. Increment: $increment ml")
                Resource.Success(Unit)
            } else {
                Log.e("WaterUpdate", "API update failed: ${response.message()}")
                Resource.Error("Gagal menyimpan data ke server: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("WaterUpdate", "Exception during water update.", e)
            Resource.Error("Terjadi kesalahan: ${e.message}")
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
                date = DateConverter.getTodayLocalFormatted(),
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

