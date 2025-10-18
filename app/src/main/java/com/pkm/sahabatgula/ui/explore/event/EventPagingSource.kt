package com.pkm.sahabatgula.ui.explore.event

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.Event
import java.io.IOException

class EventPagingSource(
    private val apiService: ApiService,
    private val query: String?,
    private val categoryId: Int?
) : PagingSource<Int, Event>() {

    init {
        Log.d("SearchDebug", "4. PAGINGSOURCE: Instance baru dibuat. Query: '$query'")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Event> {
        val page = params.key ?: 1
        return try {
            Log.d("SearchDebug", "5. PAGINGSOURCE: Memanggil API. Page: $page, Query: '$query'")
            val response = apiService.getEvents(
                page = page,
                limit = params.loadSize,
                searchQuery = query,
                categoryId = categoryId
            )

            val events = response.body()?.data ?: emptyList()

            LoadResult.Page(
                data = events,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (events.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Event>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}