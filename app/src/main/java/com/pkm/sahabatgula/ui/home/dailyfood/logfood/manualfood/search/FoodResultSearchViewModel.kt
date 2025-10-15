package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FoodResultSearchViewModel @Inject constructor(
    private val logFoodRepository: LogFoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query : String = savedStateHandle["searchQuery"]?:""
    val categoryIdString: String? = (savedStateHandle["categoryId"] ?: "")
    val categoryIdInt = categoryIdString?.toIntOrNull()

    val foods: Flow<PagingData<FoodItem>> = logFoodRepository
        .getFoodPaginated(query, categoryIdInt)
        .cachedIn(viewModelScope)
}