package com.pkm.sahabatgula.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.data.remote.model.ArticleCategory
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.remote.model.EventCategory
import com.pkm.sahabatgula.ui.explore.article.ArticlePagingSource
import com.pkm.sahabatgula.ui.explore.event.EventPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class ExploreRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getCarousels(): Resource<List<CarouselItem>> {
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

    fun getEvents(): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getEvents()
            val events = response.body()?.data ?: emptyList()
            emit(Resource.Success(events))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            emit(Resource.Error("An unknown error occurred: ${e.message}"))
        }
    }

    fun getEventsInDetail(limit: Int = 0): Flow<Resource<List<Event>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getEvents(limit = limit)

            val events = response.body()?.data ?: emptyList()
            emit(Resource.Success(events))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred: ${e.message}"))
        }
    }

    fun getArticlesInDetail(limit: Int = 0): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getArticles(limit = limit)

            val articles = response.body()?.data ?: emptyList()
            emit(Resource.Success(articles))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unknown error occurred: ${e.message}"))
        }
    }

    fun getArticles(limit: Int): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getArticles(limit = limit)
            emit(Resource.Success(response.body()?.data ?: emptyList()))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getArticlePagingData(query: String?, categoryId: Int?): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ArticlePagingSource(apiService, query, categoryId)
            }
        ).flow.map { pagingData ->
            pagingData.filter { article ->
                query.isNullOrBlank() ||
                        article.title?.contains(query!!, ignoreCase = true) == true
            }
        }
    }

    suspend fun getArticleCategories(): Resource<List<ArticleCategory>> {
        return try {
            val response = apiService.getArticleCategories()
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                Resource.Error("Gagal memuat kategori: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

    fun getEventPagingData(query: String?, categoryId: Int?): Flow<PagingData<Event>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                EventPagingSource(apiService, null, categoryId) // kirim null ke q
            }
        ).flow.map { pagingData ->
            pagingData.filter { event ->
                query.isNullOrBlank() ||
                        event.title?.contains(query!!, ignoreCase = true) == true
            }
        }
    }


    suspend fun getEventCategories(): Resource<List<EventCategory>> {
        return try {
            val response = apiService.getEventCategories()
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                Resource.Error("Gagal memuat kategori: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Terjadi kesalahan jaringan")
        }
    }

}