package com.pkm.sahabatgula.data.repository

import android.util.Log
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.data.local.room.DailySummaryDao
import com.pkm.sahabatgula.data.local.room.DailySummaryEntity
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.DailySummaryResponse
import com.pkm.sahabatgula.util.DateConverter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DATA_CHECK", "1. Raw API Response Body: $body")

//                Log.d("HomeRepository", "API Success. Response Body: $body")

//                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
//                val entity = body.toEntity(todayDate) // <-- Kirim tanggal hari ini
//                dailySummaryDao.upsert(entity)
//                Log.d("HomeRepository", "Data saved to DB with date: $todayDate. Entity: $entity")

                val serverTimestamp = body.data?.date

                if (serverTimestamp != null) {
                    // KONVERSI KE TANGGAL LOKAL
                    val localDate = DateConverter.convertUtcTimestampToLocalDateString(serverTimestamp)

                    Log.d("DATA_CHECK", "2. Date Conversion: Server UTC '$serverTimestamp' -> Local Date '$localDate'")

                    // SIMPAN KE DB DENGAN TANGGAL LOKAL YANG BENAR
                    val entity = body.toEntity(localDate)
                    dailySummaryDao.upsert(entity)

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
    fun observeDailySummary(date: String): Flow<DailySummaryEntity?> {
        Log.d("HomeRepository", "Observing daily summary untuk tanggal: $date")
        return dailySummaryDao.getSummaryByDate(date)
    }

    fun observeProfile(): Flow<ProfileEntity?> {
        return profileDao.observeProfile()
    }

    suspend fun refreshDailySummary(): Resource<Unit> {
        return try {
            val token = tokenManager.getAccessToken() ?: return Resource.Error("Token tidak ditemukan")
            val response = apiService.getDailySummary("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DATA_CHECK", "1. Raw API Response Body: $body")

                val serverTimestamp = body.data?.date
                if (serverTimestamp != null) {
                    val localDate = DateConverter.convertUtcTimestampToLocalDateString(serverTimestamp)
                    Log.d("DATA_CHECK", "2. Date Conversion: Server UTC '$serverTimestamp' -> Local Date '$localDate'")

                    val entity = body.toEntity(localDate)
                    dailySummaryDao.upsert(entity)

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



fun DailySummaryResponse.toEntity(date: String): DailySummaryEntity {
    return DailySummaryEntity(
        date = date,
        calories = this.data?.nutrients?.calories?.toInt() ?: 0,
        carbs = this.data?.nutrients?.carbs ?: 0.0,
        protein = this.data?.nutrients?.protein ?: 0.0,
        fat = this.data?.nutrients?.fat ?: 0.0,
        sugar = this.data?.nutrients?.sugar ?: 0.0,
        sodium = this.data?.nutrients?.sodium ?: 0.0,
        fiber = this.data?.nutrients?.fiber ?: 0.0,
        potassium = this.data?.nutrients?.potassium ?: 0.0,
        burned = this.data?.activities?.burned ?: 0,
        steps = this.data?.steps ?: 0,
        water = this.data?.water ?: 0
    )
}
