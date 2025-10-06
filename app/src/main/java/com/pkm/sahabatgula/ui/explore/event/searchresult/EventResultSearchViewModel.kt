package com.pkm.sahabatgula.ui.explore.event.searchresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class EventResultSearchViewModel @Inject constructor(
    private val repository: ExploreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val query: String = savedStateHandle["searchQuery"] ?: ""
    val categoryIdString: String? = (savedStateHandle["categoryId"] ?: "")
    val categoryIdInt = categoryIdString?.toIntOrNull()
    val events: Flow<PagingData<Event>> = repository
        .getEventPagingData(query, categoryIdInt)
        .cachedIn(viewModelScope)
}