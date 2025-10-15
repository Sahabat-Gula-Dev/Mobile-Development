package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogManualFoodViewModel @Inject constructor(
    private val logFoodRepository: LogFoodRepository
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<FoodCategories>>>()
    val categories: LiveData<Resource<List<FoodCategories>>> = _categories

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    private val _currentQuery= MutableStateFlow<String?>(null)
    val _currentCategory = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodPagingData: Flow<PagingData<FoodItem>> = combine(
        _currentQuery,
        _currentCategory
    ) {
        query, categoryId ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        logFoodRepository.getFoodPaginated(query, categoryId)
    }.cachedIn(viewModelScope)

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            _categories.value = logFoodRepository.getFoodCategories()
        }
    }

    fun setCategory(categoryId: Int?) {
        _currentCategory.value = categoryId
    }

}