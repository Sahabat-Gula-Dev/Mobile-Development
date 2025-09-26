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
import com.pkm.sahabatgula.data.remote.model.SummaryResponse
import com.pkm.sahabatgula.util.DateConverter
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

    suspend fun getSummary(): Resource<SummaryResponse> {
        return try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return Resource.Error("Token tidak ditemukan, silakan login kembali")
            }
            val response = apiService.getSummary("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DATA_CHECK", "1. Raw API Response Body: $body")
                val serverTimestamp = body.data?.daily?.date

                if (serverTimestamp != null) {
                    // KONVERSI KE TANGGAL LOKAL
                    val localDate = DateConverter.convertUtcTimestampToLocalDateString(serverTimestamp)

                    Log.d("DATA_CHECK", "2. Date Conversion: Server UTC '$serverTimestamp' -> Local Date '$localDate'")

                    // SIMPAN KE DB DENGAN TANGGAL LOKAL YANG BENAR
                    val entity = body

                    Log.d("HomeRepository", "Data saved to DB for local date: $localDate")
                }

                Resource.Success(body)
            } else {
                Log.e(
                    "HomeRepository",
                    "API Error. Code: ${response.code()}, Message: ${response.message()}"
                )
                Resource.Error("Gagal memuat ringkasan harian: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("HomeRepository", "Network Exception: ${e.message}", e)
            Resource.Error("Terjadi kesalahan jaringan")
        }
    }

    // Pastikan fungsi ini mengembalikan nullable untuk mencegah crash
    suspend fun getProfile(): ProfileEntity? { // <-- Ubah ke ProfileEntity? (nullable)
        return profileDao.getProfile()
    }

    // Fungsi ini tetap berguna jika Anda ingin mengamati perubahan dari DB
    fun observeDailySummary(date: String): Flow<SummaryEntity?> {
        Log.d("HomeRepository", "Observing DAILY summary untuk tanggal: $date")
        return summaryDao.getSummaryByDate("DAILY", date)
    }


    fun observeProfile(): Flow<ProfileEntity?> {
        return profileDao.observeProfile()
    }

    suspend fun refreshDailySummary(): Resource<Unit> {
        return try {
            val token = tokenManager.getAccessToken() ?: return Resource.Error("Token tidak ditemukan")
            val response = apiService.getSummary("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DATA_CHECK", "1. Raw API Response Body: $body")

                val serverTimestamp = body.data?.daily?.date
                if (serverTimestamp != null) {
                    val localDate = DateConverter.convertUtcTimestampToLocalDateString(serverTimestamp)
                    Log.d("DATA_CHECK", "2. Date Conversion: Server UTC '$serverTimestamp' -> Local Date '$localDate'")

                    val entity = body.data.toEntity()
                    summaryDao.upsert(entity)

                    Log.d("HomeRepository", "Data refreshed and saved for local date: $localDate")
                Log.d("DATA_CHECK", "3. Entity to be Saved: $entity")
                }

                Resource.Success(Unit)
            } else {
                Resource.Error("Gagal memperbarui data: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Terjadi kesalahan jaringan saat memperbarui data")
        }
    }
}

fun SummaryData.toEntity(): List<SummaryEntity> {
    val entities = mutableListOf<SummaryEntity>()

    daily?.let { d ->
        entities.add(
            SummaryEntity(
                type = "DAILY",
                date = d.date,
                calories = d.nutrients?.calories,
                carbs = d.nutrients?.carbs,
                protein = d.nutrients?.protein,
                fat = d.nutrients?.fat,
                sugar = d.nutrients?.sugar,
                sodium = d.nutrients?.sodium,
                fiber = d.nutrients?.fiber,
                potassium = d.nutrients?.potassium,
                burned = d.activities?.burned,
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
                calories = w.nutrients?.calories,
                carbs = w.nutrients?.carbs,
                protein = w.nutrients?.protein,
                fat = w.nutrients?.fat,
                sugar = w.nutrients?.sugar,
                sodium = w.nutrients?.sodium,
                fiber = w.nutrients?.fiber,
                potassium = w.nutrients?.potassium,
                burned = w.activities?.burned,
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
                calories = m.nutrients?.calories,
                carbs = m.nutrients?.carbs,
                protein = m.nutrients?.protein,
                fat = m.nutrients?.fat,
                sugar = m.nutrients?.sugar,
                sodium = m.nutrients?.sodium,
                fiber = m.nutrients?.fiber,
                potassium = m.nutrients?.potassium,
                burned = m.activities?.burned,
                steps = m.steps,
                water = m.water
            )
        )
    }

    return entities
}

