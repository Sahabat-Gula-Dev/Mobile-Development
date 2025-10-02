package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.ActivitiesDataItem

class ActivityPagingSource(
    private val apiService: ApiService,
    private val query: String?,
    private val categoryId: Int?
): PagingSource<Int, ActivitiesDataItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ActivitiesDataItem> {
        val page = params.key?:1
        return try {
            val response = apiService.getActivities(
                page = page,
                limit = params.loadSize,
                query = query,
                categoryId = categoryId
            )
            Log.d("ActivityPagingSource", "API response: ${response.code()} size=${response.body()?.data?.size}")

            val activities = response.body()?.data?:emptyList()

            LoadResult.Page(
                data = activities,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (activities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("ActivityPagingSource", "Error load page=$page", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ActivitiesDataItem>): Int? {
        return state.anchorPosition?.let{ anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}