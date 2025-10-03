package com.pkm.sahabatgula.data.repository

import android.net.http.HttpException
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.data.remote.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class ExploreRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getCarusels(): Resource<List<CarouselItem>> {
        return try {
            val response = apiService.getCarousels()
            if(response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi Kesalahan")
        }
    }

    suspend fun getEvents(): Flow<Resource<List<Event>>> = flow {
        // 1. Emit Loading terlebih dahulu
        emit(Resource.Loading())

        try {
            val response = apiService.getEvents()
            val events = response.body()?.data ?: emptyList()
            // 2. Jika sukses, emit Success
            emit(Resource.Success(events))
        } catch (e: IOException) {
            // Error koneksi
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            // Error umum lainnya
            emit(Resource.Error("An unknown error occurred: ${e.message}"))
        }
    }
}