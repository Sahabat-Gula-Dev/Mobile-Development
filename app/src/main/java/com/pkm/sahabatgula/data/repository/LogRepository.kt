//package com.pkm.sahabatgula.data.repository
//
//import com.pkm.sahabatgula.core.Resource
//import com.pkm.sahabatgula.data.local.TokenManager
//import com.pkm.sahabatgula.data.local.room.FoodLogDao
//import com.pkm.sahabatgula.data.local.room.FoodLogSummaryEntity
//import com.pkm.sahabatgula.data.local.room.ProfileDao
//import com.pkm.sahabatgula.data.remote.api.ApiService
//import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
//import com.pkm.sahabatgula.data.remote.model.LogFoodRequest
//import com.pkm.sahabatgula.data.remote.model.Totals
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOf
//import java.time.LocalDate
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import javax.inject.Inject
//import javax.inject.Singleton
//
//
//@Singleton
//class LogRepository @Inject constructor(
//    private val apiService: ApiService,
//    private val tokenManager: TokenManager,
//    private val foodLogDao: FoodLogDao
//) {
//
//    suspend fun logFoodSummary(
//        items: List<FoodItemRequest>,
//        date: String? = null
//    ): Resource<Totals> {
//        val targetDate = date ?: LocalDate.now(ZoneId.of("Asia/Makassar"))
//            .format(DateTimeFormatter.ISO_DATE)
//        val requestBody = LogFoodRequest(foods = items)
//        return try {
//            val token = tokenManager.getAccessToken() ?: return Resource.Error("Token tidak ditemukan")
//            val response = apiService.logFood("Bearer $token", requestBody)
//            if (response.isSuccessful && response.body() != null) {
//                val totals = response.body()!!.data.totals
//                val foodLogSummaryEntity = totals.toEntity(targetDate)
//                foodLogDao.upsertLogFood(foodLogSummaryEntity)
//                Resource.Success(totals)
//            } else {
//                Resource.Error("Gagal mengambil data log makanan: ${response.message()}")
//            }
//        } catch (e: Exception){
//            Resource.Error("Gagal mengambil data log makanan: ${e.message}")
//        }
//    }
//}
//
//fun Totals.toEntity(date: String): FoodLogSummaryEntity {
//    return FoodLogSummaryEntity(
//        date = date,
//        calories = this.calories.toInt(),
//        carbs = this.carbs,
//        protein = this.protein,
//        fat = this.fat,
//        sugar = this.sugar,
//        sodium = this.sodium,
//        fiber = this.fiber,
//        potassium = this.potassium
//    )
//
//}