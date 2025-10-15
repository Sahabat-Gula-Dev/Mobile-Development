package com.pkm.sahabatgula.ui.settings.helpcenter

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.ApiService // Ganti dengan path ApiService-mu
import com.pkm.sahabatgula.data.remote.model.FaqItem
import java.io.IOException

class FaqPagingSource(
    private val apiService: ApiService,
    private val query: String?,
    private val categoryId: Int?
) : PagingSource<Int, FaqItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FaqItem> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getFaqs(
                page = page,
                limit = params.loadSize,
                quary = query,
                categoryId = categoryId
            )

            val faqs = response.body()?.data?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = faqs,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (faqs.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FaqItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}