package com.pkm.sahabatgula.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pkm.sahabatgula.BuildConfig
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.api.NewsApiService
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.data.remote.model.ArticleCategory
import com.pkm.sahabatgula.data.remote.model.ArticleItem
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.remote.model.EventCategory
import com.pkm.sahabatgula.data.remote.model.NewsItem
import com.pkm.sahabatgula.ui.explore.article.ArticlePagingSource
import com.pkm.sahabatgula.ui.explore.event.EventPagingSource
import com.pkm.sahabatgula.ui.explore.news.NewsPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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

    suspend fun getArticleDetail(id: String): Resource<ArticleItem> {
        return try {
            val response = apiService.getArticleDetail(id)

            if (response.isSuccessful && response.body() != null) {
                val article = response.body()?.data!!.article
                Log.d("Debug SCAN REPO", "getFoodDetail: $article")
                Resource.Success(article)
            } else {
                Resource.Error("Gagal mengambil detail artikel hari : ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("Debug SCAN REPO", "Error: ${e.message}", e)
            Resource.Error(e.message ?: "Terjadi kesalahan tidak diketahui")
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
        ).flow
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
                EventPagingSource(apiService, query, categoryId)
            }
        ).flow
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