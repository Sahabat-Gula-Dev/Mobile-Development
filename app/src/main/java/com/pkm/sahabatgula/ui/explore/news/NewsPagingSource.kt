package com.pkm.sahabatgula.ui.explore.news

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.NewsApiService
import com.pkm.sahabatgula.data.remote.model.NewsItem

class NewsPagingSource(
    private val apiService: NewsApiService,
    private val rssUrl: String,
    private val apiKey: String
) : PagingSource<Int, NewsItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsItem> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val response = apiService.getNewsFromRss(
                rssUrl = rssUrl,
                apiKey = apiKey,
                count = 30
            )

            if (response.isSuccessful) {
                val data = response.body()?.items ?: emptyList()

                val fromIndex = (page - 1) * pageSize
                val toIndex = minOf(page * pageSize, data.size)
                val pageData = if (fromIndex < data.size) data.subList(fromIndex, toIndex) else emptyList()

                LoadResult.Page(
                    data = pageData,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (toIndex < data.size) page + 1 else null
                )
            } else {
                LoadResult.Error(Exception("Gagal: ${response.message()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsItem>): Int? = state.anchorPosition
}
