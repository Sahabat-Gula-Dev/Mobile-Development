package com.pkm.sahabatgula.ui.explore.article.detailarticle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.Article
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailArticleViewModel @Inject constructor(private val repository: ExploreRepository): ViewModel() {

    private val _relatedArticles = MutableStateFlow<Resource<List<Article>>>(Resource.Loading())
    val relatedArticles: StateFlow<Resource<List<Article>>> = _relatedArticles

    private val RANDOM_FETCH_LIMIT = 4
    private val DISPLAY_LIMIT = 3

    fun loadRelatedArticles(currentArticleId: String) {
        viewModelScope.launch {
            repository.getArticlesInDetail(limit = RANDOM_FETCH_LIMIT).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _relatedArticles.value = Resource.Loading()
                    is Resource.Success -> {
                        val randomArticles = resource.data
                            ?.shuffled()
                            ?.filter { it.id != currentArticleId }
                            ?.take(DISPLAY_LIMIT)
                        _relatedArticles.value =
                            Resource.Success(randomArticles) as Resource<List<Article>>
                    }

                    is Resource.Error -> _relatedArticles.value = Resource.Error(resource.message)
                }
            }
        }
    }

}