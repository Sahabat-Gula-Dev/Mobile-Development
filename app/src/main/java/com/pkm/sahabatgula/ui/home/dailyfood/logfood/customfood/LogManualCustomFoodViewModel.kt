package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogManualCustomFoodViewModel @Inject constructor(
    private val logFoodRepository: LogFoodRepository
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<FoodCategories>>>()
    val categories: LiveData<Resource<List<FoodCategories>>> = _categories

    private val _currentQuery= MutableStateFlow<String?>(null)
    val _currentCategory = MutableStateFlow<Int?>(null)

    private val _selectedFoodIds = MutableStateFlow<Set<String>>(emptySet())
    private val _expandedFoodId = MutableStateFlow<String?>(null)

    val _logFoodStatus = MutableLiveData<Resource<Unit>>()
    val logFoodStatus: LiveData<Resource<Unit>> = _logFoodStatus

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodPagingData: Flow<PagingData<FoodItem>> = combine(
        _currentQuery,
        _currentCategory,
        _selectedFoodIds,
        _expandedFoodId
    ) { query, categoryId, selectedIds, expandedId ->
        SearchParameters(query, categoryId, selectedIds, expandedId)
    }.flatMapLatest { params ->
        logFoodRepository.getFoodPaginated(params.query, params.categoryId)
            .map { pagingData ->
                pagingData.map { foodItem ->
                    foodItem.copy(
                        isSelected = params.selectedIds.contains(foodItem.id),
                        isExpanded = foodItem.id == params.expandedId
                    )
                }
            }
    }.cachedIn(viewModelScope) // 4. Terapkan cachedIn pada hasil AKHIR Flow

    // Helper data class untuk membuat kode lebih bersih
    private data class SearchParameters(
        val query: String?,
        val categoryId: Int?,
        val selectedIds: Set<String>,
        val expandedId: String?
    )

    init {
        fetchCategories()
    }

    fun toggleFoodSelection(foodItem: FoodItem) {
        val currentSelected = _selectedFoodIds.value.toMutableSet()
        if (currentSelected.contains(foodItem.id)) {
            currentSelected.remove(foodItem.id)
        } else {
            currentSelected.add(foodItem.id)
        }
        _selectedFoodIds.value = currentSelected
    }

    fun onExpandClicked(foodItem: FoodItem) {
        _expandedFoodId.value = if (_expandedFoodId.value == foodItem.id) {
            null // Jika item yang sama diklik lagi, tutup (collapse)
        } else {
            foodItem.id // Jika item lain diklik, buka (expand)
        }
    }
    private fun fetchCategories() {
        viewModelScope.launch {
            _categories.postValue(Resource.Loading())
            _categories.postValue(logFoodRepository.getFoodCategories())
        }
    }

    fun searchFood(query: String?) {
        _currentQuery.value = query
    }

    fun setCategory(categoryId: Int?) {
        _currentCategory.value = categoryId
    }

    fun logSelectedFoods() {
        viewModelScope.launch {
            val selectedIds = _selectedFoodIds.value
            if (selectedIds.isEmpty()) {
                _logFoodStatus.value = Resource.Error("Pilih minimal satu makanan.")
                return@launch
            }

            _logFoodStatus.value = Resource.Loading()

             val requestItems = selectedIds.map { foodId ->
                FoodItemRequest(foodId = foodId, portion = 1) // porsi default adalah 1
            }

            val result = logFoodRepository.logFood(requestItems)
            _logFoodStatus.value = result
        }
    }

}