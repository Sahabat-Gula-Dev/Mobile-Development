package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.SearchParameters
import com.pkm.sahabatgula.data.remote.model.ActivitiesDataItem
import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.data.remote.model.LogActivityItemRequest
import com.pkm.sahabatgula.data.repository.LogActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map


@HiltViewModel
class LogActivityViewModel @Inject constructor(
    private val logActivityRepository: LogActivityRepository
) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<ActivityCategories>>>()
    val categories: LiveData<Resource<List<ActivityCategories>>> = _categories

    private val _currentQuery = MutableStateFlow<String?>(null)
    val _currentCategory = MutableStateFlow<Int?>(null)

    private val _selectedActivityIds = MutableStateFlow<Set<String>>(emptySet())
    private val _expandedActivityId = MutableStateFlow<String?>(null)

    val _logActivityStatus = MutableLiveData<Resource<Unit>>()
    val logActivityStatus: LiveData<Resource<Unit>> = _logActivityStatus

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityPagingData: Flow<PagingData<ActivitiesDataItem>> = combine(
        _currentQuery,
        _currentCategory,
        _selectedActivityIds,
        _expandedActivityId
    ) { query, categoryId, selectedIds, expandedId ->
        SearchParameters(query, categoryId, selectedIds, expandedId)
    }.flatMapLatest { params ->
        logActivityRepository.getActivityPaginated(params.query, params.categoryId)
            .map { pagingData ->
                pagingData.map { activity ->
                    activity.copy(
                        isSelected = params.selectedIds.contains(activity.id),
                        isExpanded = activity.id == params.expandedId
                    )
                }
            }
    }.cachedIn(viewModelScope)

    init {
        fetchCategories()
    }

    fun toggleActivitySelection(activityItem: ActivitiesDataItem) {
        val currentSelected = _selectedActivityIds.value.toMutableSet()
        if (currentSelected.contains(activityItem.id)) {
            currentSelected.remove(activityItem.id)
        } else {
            currentSelected.add(activityItem.id)
        }
        _selectedActivityIds.value = currentSelected
    }

    fun onExpandClicked(activityItem: ActivitiesDataItem) {
        _expandedActivityId.value = if (_expandedActivityId.value == activityItem.id) {
            null // Jika item yang sama diklik lagi, tutup (collapse)
        } else {
            activityItem.id // Jika item lain diklik, buka (expand)
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
            val selectedIds = _selectedActivityIds.value
            if (selectedIds.isEmpty()) {
                _logActivityStatus.value = Resource.Error("Pilih minimal satu makanan.")
                return@launch
            }

            _logActivityStatus.value = Resource.Loading()

            val requestItems = selectedIds.map { activityId ->
                LogActivityItemRequest(activityId = activityId) // porsi default adalah 1
            }

            val result = logActivityRepository.logActivity(requestItems)
            _logActivityStatus.value = result
        }
    }
}