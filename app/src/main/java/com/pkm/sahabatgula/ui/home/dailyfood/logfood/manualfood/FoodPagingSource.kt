package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pkm.sahabatgula.data.remote.api.ApiService
import com.pkm.sahabatgula.data.remote.model.FoodItem

class FoodPagingSource(
    private val apiService: ApiService,
    private val query: String?,
    private val categoryId: Int?
): PagingSource<Int, FoodItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FoodItem> {
        val page = params.key?:1
        return try {
            val response = apiService.getFoods(
                page = page,
                limit = params.loadSize,
                query = query,
                categoryId = categoryId
            )
            Log.d("FoodPagingSource", "API response: ${response.code()} size=${response.body()?.data?.size}")

            val foods = response.body()?.data?:emptyList()

            LoadResult.Page(
                data = foods,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (foods.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("FoodPagingSource", "Error load page=$page", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FoodItem>): Int? {
        return state.anchorPosition?.let{ anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}