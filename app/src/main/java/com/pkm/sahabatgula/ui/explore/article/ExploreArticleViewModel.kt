package com.pkm.sahabatgula.ui.explore.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.ArticleCategory
import com.pkm.sahabatgula.data.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreArticleViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<ArticleCategory>>>(Resource.Loading())
    val categories: StateFlow<Resource<List<ArticleCategory>>> = _categories

    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    init {
        fetchArticleCategories()
    }
    val articles = combine(_searchQuery, _selectedCategoryId) { query, categoryId ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        repository.getArticlePagingData(query, categoryId).cachedIn(viewModelScope)
    }

    private fun fetchArticleCategories() {
        viewModelScope.launch {
            _categories.value = repository.getArticleCategories()
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }
}