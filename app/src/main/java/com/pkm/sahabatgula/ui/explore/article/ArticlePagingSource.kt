package com.pkm.sahabatgula.ui.explore.article

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Article
import java.io.IOException

class ArticlePagingSource(
    private val apiService: ApiService,
    private val query: String?,
    private val categoryId: Int?
) : PagingSource<Int, Article>() {

    init {
        Log.d("SearchDebug", "4. PAGINGSOURCE: Instance baru dibuat. Query: '$query'")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            Log.d("SearchDebug", "5. PAGINGSOURCE: Memanggil API. Page: $page, Query: '$query'")
            val response = apiService.getArticles(
                page = page,
                limit = params.loadSize,
                searchQuery = query,
                categoryId = categoryId
            )

            val articles = response.body()?.data ?: emptyList()

            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (articles.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}