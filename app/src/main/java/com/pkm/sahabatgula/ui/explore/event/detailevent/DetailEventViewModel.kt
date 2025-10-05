package com.pkm.sahabatgula.ui.explore.event.detailevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailEventViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    private val _relatedEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Loading())
    val relatedEvents: StateFlow<Resource<List<Event>>> = _relatedEvents

    private val RANDOM_FETCH_LIMIT = 4
    private val DISPLAY_LIMIT = 3

    fun loadRelatedEvents(currentEventId: String) {
        viewModelScope.launch {
            repository.getEventsInDetail(limit = RANDOM_FETCH_LIMIT).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _relatedEvents.value = Resource.Loading()
                    is Resource.Success -> {
                        val randomEvents = resource.data
                            ?.shuffled()
                            ?.filter { it.id != currentEventId }
                            ?.take(DISPLAY_LIMIT)
                        _relatedEvents.value =
                            Resource.Success(randomEvents) as Resource<List<Event>>
                    }

                    is Resource.Error -> _relatedEvents.value = Resource.Error(resource.message)
                }
            }
        }
    }
}