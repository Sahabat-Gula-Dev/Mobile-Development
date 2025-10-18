package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.ActivitiesDataItem
import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.data.remote.model.LogActivityItemRequest
import com.pkm.sahabatgula.data.repository.LogActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogActivityViewModel @Inject constructor(
    private val logActivityRepository: LogActivityRepository
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<ActivityCategories>>>()
    val categories: LiveData<Resource<List<ActivityCategories>>> = _categories

    private val _currentQuery = MutableStateFlow<String?>(null)
    private val _currentCategory = MutableStateFlow<Int?>(null)

    private val selectedActivityIds = mutableSetOf<String>()
    private var expandedActivityId: String? = null

    private val _selectedActivityIdsState = MutableStateFlow<Set<String>>(emptySet())
    val selectedActivityIdsState: StateFlow<Set<String>> = _selectedActivityIdsState

    private val _logActivityStatus = MutableLiveData<Resource<Unit>>()
    val logActivityStatus: LiveData<Resource<Unit>> = _logActivityStatus

    private val cachedActivityList = mutableListOf<ActivitiesDataItem>()

    init {
        fetchCategories()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityPagingData: Flow<PagingData<ActivitiesDataItem>> = combine(
        _currentQuery,
        _currentCategory
    ) { query, categoryId ->
        query to categoryId
    }.flatMapLatest { (query, categoryId) ->
        logActivityRepository.getActivityPaginated(query, categoryId)
            .map { pagingData ->
                pagingData.filter { item ->
                    query.isNullOrBlank() || item.name.contains(query, ignoreCase = true)
                }.map { item ->
                    item.copy(
                        isSelected = selectedActivityIds.contains(item.id),
                        isExpanded = item.id == expandedActivityId
                    ).also { updated ->
                        if (cachedActivityList.none { it.id == updated.id }) {
                            cachedActivityList.add(updated)
                        }
                    }
                }
            }
    }.cachedIn(viewModelScope)

    fun toggleActivitySelection(activityItem: ActivitiesDataItem) {
        if (selectedActivityIds.contains(activityItem.id)) {
            selectedActivityIds.remove(activityItem.id)
            activityItem.isSelected = false
        } else {
            selectedActivityIds.add(activityItem.id)
            activityItem.isSelected = true
        }
        _selectedActivityIdsState.value = selectedActivityIds.toSet()
    }

    fun onExpandClicked(activityItem: ActivitiesDataItem) {
        expandedActivityId = if (expandedActivityId == activityItem.id) {
            activityItem.isExpanded = false
            null
        } else {
            activityItem.isExpanded = true
            activityItem.id
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            _categories.postValue(Resource.Loading())
            _categories.postValue(logActivityRepository.getActivityCategories())
        }
    }

    fun searchActivity(query: String?) {
        _currentQuery.value = query
    }

    fun setCategory(categoryId: Int?) {
        _currentCategory.value = categoryId
    }

    fun logSelectedActivities() {
        viewModelScope.launch {
            if (selectedActivityIds.isEmpty()) {
                _logActivityStatus.value = Resource.Error("Pilih minimal satu aktivitas.")
                return@launch
            }
            _logActivityStatus.value = Resource.Loading()

            val requestItems = selectedActivityIds.map { activityId ->
                LogActivityItemRequest(activityId = activityId)
            }
            val result = logActivityRepository.logActivity(requestItems)
            _logActivityStatus.value = result
        }
    }

    fun getSelectedActivityNamesAndCalories(): Pair<List<String>, Int> {
        val selected = cachedActivityList.filter { selectedActivityIds.contains(it.id) }
        val names = selected.map { it.name }
        val totalCalories = selected.sumOf { it.caloriesBurned }
        return names to totalCalories
    }

    fun getSelectedActivitiesSummary(): SelectedActivitySummary {
        val selected = cachedActivityList.filter { selectedActivityIds.contains(it.id) }
        val totalCalories = selected.sumOf { it.caloriesBurned }
        return SelectedActivitySummary(
            name = selected.firstOrNull()?.name ?: "",
            duration = selected.firstOrNull()?.duration ?: 0,
            durationUnit = selected.firstOrNull()?.durationUnit ?: "",
            totalCalories = totalCalories
        )
    }

    data class SelectedActivitySummary(
        val name: String,
        val duration: Int,
        val durationUnit: String,
        val totalCalories: Int
    )
}
