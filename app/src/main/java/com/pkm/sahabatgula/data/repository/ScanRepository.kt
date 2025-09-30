package com.pkm.sahabatgula.data.repository

import android.util.Log
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Food
import com.pkm.sahabatgula.data.remote.model.PredictionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ScanRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun predictImage(imageFile: File): Resource<PredictionResponse>{
        return try {
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )
            val response = apiService.predictImage(multipartBody)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message?: "Terjadi kesalahan tidak diketahui")
        }
    }

    suspend fun getFoodDetail(id: String): Resource<Food> {
        return try {
            val response = apiService.getFoodDetailById(id)

            if (response.isSuccessful && response.body() != null) {
                val food = response.body()!!.data.food
                Log.d("Debug SCAN REPO", "getFoodDetail: $food")
                Resource.Success(food)
            } else {
                Resource.Error("Gagal: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("Debug SCAN REPO", "Error: ${e.message}", e)
            Resource.Error(e.message ?: "Terjadi kesalahan tidak diketahui")
        }
    }

}