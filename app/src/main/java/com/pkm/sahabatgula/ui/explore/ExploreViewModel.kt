package com.pkm.sahabatgula.ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.data.remote.model.CarouselItem
import com.pkm.sahabatgula.data.remote.model.Event
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    private val _carouselItems = MutableLiveData<Resource<List<CarouselItem>>>()
    val carouselItems: LiveData<Resource<List<CarouselItem>>> = _carouselItems

    private val _eventState = MutableStateFlow<Resource<List<Event>>>(Resource.Loading())
    val eventState: StateFlow<Resource<List<Event>>> = _eventState

    private val _articleState = MutableStateFlow<Resource<List<Article>>>(Resource.Loading())
    val articleState: StateFlow<Resource<List<Article>>> = _articleState


    init {
        fetchCarousels()
        fetchEvents()
        fetchArticles(limit = 3)
    }

    private fun fetchCarousels() {
        viewModelScope.launch {
            _carouselItems.value = Resource.Loading()
            _carouselItems.value = repository.getCarusels()
        }
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            repository.getEvents()
                .collect { resource ->
                    _eventState.value = resource
                }
        }
    }

    fun fetchArticles(limit: Int) {
        viewModelScope.launch {
            repository.getArticles(limit).collect { result ->
                _articleState.value = result
            }
        }
    }


}