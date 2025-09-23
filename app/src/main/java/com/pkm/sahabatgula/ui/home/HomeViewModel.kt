package com.pkm.sahabatgula.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val homeRepository: HomeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asSharedFlow() as StateFlow<HomeState>

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        fetchHOmeData()
    }

    fun fetchHOmeData() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading
            val profile = homeRepository.getProfile()

//            if(profile == null) {
//                _uiState.value = HomeState.Error("Gagal memuat profil pengguna")
//                _effect.emit(HomeEffect.ShowToast("Data Profile tidak ditemukan"))
//                return@launch
//            }

            when (val summaryResponse = homeRepository.getDailySummary()) {
                is Resource.Success -> {
                    _uiState.value = HomeState.Success(profile, summaryResponse.data!!)
                }
                is Resource.Error -> {
                    _uiState.value = HomeState.Error(summaryResponse.message?: "Terjadi Kesalahan")

                }
                else -> {}
            }
        }
    }
}