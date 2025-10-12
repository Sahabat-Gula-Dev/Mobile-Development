package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.SearchParameters
import com.pkm.sahabatgula.data.remote.model.Food
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import com.pkm.sahabatgula.data.repository.ScanRepository
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
    private val logFoodRepository: LogFoodRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<FoodCategories>>>()
    val categories: LiveData<Resource<List<FoodCategories>>> = _categories

    private val _currentQuery = MutableStateFlow<String?>(null)
    val _currentCategory = MutableStateFlow<Int?>(null)

    private val _selectedFoodIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFoodIds = _selectedFoodIds
    private val _expandedFoodId = MutableStateFlow<String?>(null)

    private val _logFoodStatus = MutableLiveData<Resource<Unit>>()
    val logFoodStatus: LiveData<Resource<Unit>> = _logFoodStatus

    private val cachedFoodList = mutableListOf<FoodItem>()

    private val _foodDetail = MutableLiveData<Resource<Food>>()
    val foodDetail: MutableLiveData<Resource<Food>> = _foodDetail


    fun fetchFoodDetail(id: String?) {
        if (id == null) {
            _foodDetail.value = Resource.Error("Food ID is missing.")
            return
        }
        viewModelScope.launch {
            _foodDetail.value = Resource.Loading()
            try {
                val response = scanRepository.getFoodDetail(id)
                _foodDetail.value = response
            } catch (e: Exception) {
                _foodDetail.value = Resource.Error(e.message ?: "An unknown error occurred")
                Log.e("DetailFoodViewModel", "Error fetching food detail", e)
            }
        }
    }

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
                    val updated = foodItem.copy(
                        isSelected = params.selectedIds.contains(foodItem.id),
                        isExpanded = foodItem.id == params.expandedId
                    )
                    if (cachedFoodList.none { it.id == updated.id }) {
                        cachedFoodList.add(updated)
                    }
                    updated
                }
            }
    }.cachedIn(viewModelScope)


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
            null
        } else {
            foodItem.id
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
                FoodItemRequest(foodId = foodId, portion = 1) // ✅ CHANGED: porsi default 1
            }

            val result = logFoodRepository.logFood(requestItems)
            _logFoodStatus.value = result
        }
    }

    fun getSelectedFoodNamesAndCalories(): Pair<List<String>, Int> {
        val selectedIds = _selectedFoodIds.value
        val allFoods = foodPagingData
        return cachedFoodList
            .filter { selectedIds.contains(it.id) }
            .let { selectedFoods ->
                val names = selectedFoods.map { it.name }
                val totalCalories = selectedFoods.sumOf { it.calories.toInt() }
                names to totalCalories
            }
    }

    suspend fun getSelectedFoodsSummaryFromDetail(): SelectedFoodSummary {
        val selectedIds = _selectedFoodIds.value
        if (selectedIds.isEmpty()) {
            return SelectedFoodSummary(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0)
        }

        var totalCalories = 0
        var totalCarbo = 0
        var totalProtein = 0
        var totalFat = 0
        var totalSugar = 0.0
        var totalSodium = 0.0
        var totalFiber = 0.0
        var totalKalium = 0.0

        for (id in selectedIds) {
            try {
                val result = scanRepository.getFoodDetail(id)
                if (result is Resource.Success && result.data != null) {
                    val food = result.data
                    totalCalories += food.calories?.toInt() ?: 0
                    totalCarbo += food.carbs?.toInt() ?: 0
                    totalProtein += food.protein?.toInt() ?: 0
                    totalFat += food.fat?.toInt() ?: 0
                    totalSugar += food.sugar ?: 0.0
                    totalSodium += food.sodium ?: 0.0
                    totalFiber += food.fiber ?: 0.0
                    totalKalium += food.potassium ?: 0.0
                }
            } catch (e: Exception) {
                // Biar tidak stop semua kalau salah satu gagal
                e.printStackTrace()
            }
        }

        return SelectedFoodSummary(
            calories = totalCalories,
            carbo = totalCarbo,
            protein = totalProtein,
            fat = totalFat,
            sugar = totalSugar,
            sodium = totalSodium,
            fiber = totalFiber,
            kalium = totalKalium
        )
    }


    data class SelectedFoodSummary(
        val calories: Int,
        val carbo: Int,
        val protein: Int,
        val fat: Int,
        val sugar: Double,
        val sodium: Double,
        val fiber: Double,
        val kalium: Double
    )


}
