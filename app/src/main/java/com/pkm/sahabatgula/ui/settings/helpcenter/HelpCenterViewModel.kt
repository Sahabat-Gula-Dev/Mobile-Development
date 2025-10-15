package com.pkm.sahabatgula.ui.settings.helpcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pkm.sahabatgula.data.remote.model.FaqCategories
import com.pkm.sahabatgula.data.remote.model.FaqItem
import com.pkm.sahabatgula.data.repository.FaqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor(private val faqRepository: FaqRepository) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)

    private val _categories = MutableStateFlow<List<FaqCategories>>(emptyList())
    val categories: StateFlow<List<FaqCategories>> = _categories.asStateFlow()

    init {
        fetchFaqCategories()
    }

    private fun fetchFaqCategories() {
        viewModelScope.launch {
            try {
                val response = faqRepository.getFaqCategories()
                if (response.isSuccessful) {
                    _categories.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val faqs: Flow<PagingData<FaqItem>> = _selectedCategoryId.flatMapLatest { categoryId ->
        faqRepository.getFaqsStream(categoryId)
    }.cachedIn(viewModelScope)
}