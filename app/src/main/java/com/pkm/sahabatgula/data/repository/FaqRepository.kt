package com.pkm.sahabatgula.data.repository
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.FaqCategoryListResponse
import com.pkm.sahabatgula.data.remote.model.FaqItem
import com.pkm.sahabatgula.ui.settings.helpcenter.FaqPagingSource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class FaqRepository @Inject constructor(
    private val apiService: ApiService
) {

    fun getFaqsStream(categoryId: Int?): Flow<PagingData<FaqItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { FaqPagingSource(apiService, query = null, categoryId = categoryId) }
        ).flow
    }

    suspend fun getFaqCategories(): Response<FaqCategoryListResponse> { // Ganti model jika perlu
        return apiService.getFaqCategories()
    }
}