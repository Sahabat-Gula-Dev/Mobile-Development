package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.data.remote.model.FoodItem
import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import com.pkm.sahabatgula.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogManualCustomFoodViewModel @Inject constructor(
    private val logFoodRepository: LogFoodRepository,
    private val scanRepository: ScanRepository,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<FoodCategories>>>()
    val categories: LiveData<Resource<List<FoodCategories>>> = _categories

    private val _currentQuery = MutableStateFlow<String?>(null)
    private val _currentCategory = MutableStateFlow<Int?>(null)

    private val selectedFoodIds = mutableSetOf<String>()
    private var expandedFoodId: String? = null

    private val _selectedFoodIdsState = MutableStateFlow<Set<String>>(emptySet())
    val selectedFoodIdsState: StateFlow<Set<String>> = _selectedFoodIdsState

    private val _logFoodStatus = MutableLiveData<Resource<Unit>>()
    val logFoodStatus: LiveData<Resource<Unit>> = _logFoodStatus

    private val cachedFoodList = mutableListOf<FoodItem>()

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile: StateFlow<ProfileEntity?> get() = _profile

    init {
        fetchCategories()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = profileDao.getProfile()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val foodPagingData: Flow<PagingData<FoodItem>> = combine(
        _currentQuery,
        _currentCategory
    ) { query, categoryId ->
        query to categoryId
    }.flatMapLatest { (query, categoryId) ->
        logFoodRepository.getFoodPaginated(query, categoryId)
            .map { pagingData ->
                pagingData.filter { item ->
                    query.isNullOrBlank() || item.name.contains(query, ignoreCase = true)
                }.map { item ->
                    // apply selected/expanded state manual
                    item.copy(
                        isSelected = selectedFoodIds.contains(item.id),
                        isExpanded = item.id == expandedFoodId
                    ).also { updated ->
                        if (cachedFoodList.none { it.id == updated.id }) {
                            cachedFoodList.add(updated)
                        }
                    }
                }
            }
    }.cachedIn(viewModelScope)

    fun toggleFoodSelection(foodItem: FoodItem) {
        if (selectedFoodIds.contains(foodItem.id)) {
            selectedFoodIds.remove(foodItem.id)
            foodItem.isSelected = false
        } else {
            selectedFoodIds.add(foodItem.id)
            foodItem.isSelected = true
        }
        _selectedFoodIdsState.value = selectedFoodIds.toSet()
    }

    fun onExpandClicked(foodItem: FoodItem) {
        expandedFoodId = if (expandedFoodId == foodItem.id) {
            foodItem.isExpanded = false
            null
        } else {
            foodItem.isExpanded = true
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
            if (selectedFoodIds.isEmpty()) {
                _logFoodStatus.value = Resource.Error("Pilih minimal satu makanan.")
                return@launch
            }

            _logFoodStatus.value = Resource.Loading()

            val requestItems = selectedFoodIds.map { foodId ->
                FoodItemRequest(foodId = foodId, portion = 1)
            }

            val result = logFoodRepository.logFood(requestItems)
            _logFoodStatus.value = result
        }
    }

    fun getSelectedFoodNamesAndCalories(): Pair<List<String>, Int> {
        return cachedFoodList
            .filter { selectedFoodIds.contains(it.id) }
            .let { selectedFoods ->
                val names = selectedFoods.map { it.name }
                val totalCalories = selectedFoods.sumOf { it.calories.toInt() }
                names to totalCalories
            }
    }

    suspend fun getSelectedFoodsSummaryFromDetail(): SelectedFoodSummary {
        if (selectedFoodIds.isEmpty()) {
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

        for (id in selectedFoodIds) {
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
