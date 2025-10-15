package com.pkm.sahabatgula.ui.explore.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.EventCategory
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreEventViewModel @Inject constructor(
    private val repository: ExploreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<EventCategory>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<EventCategory>>> = _categories

    private val _searchQuery = MutableStateFlow<String?>(null)

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId


    init {
        fetchEventCategories()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val events = combine(_searchQuery, _selectedCategoryId) { query, categoryId ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        repository.getEventPagingData(query, categoryId).cachedIn(viewModelScope)
    }

    private fun fetchEventCategories() {
        viewModelScope.launch {
            _categories.value = Resource.Loading()
            _categories.value = repository.getEventCategories()
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }
}