package com.pkm.sahabatgula.ui.settings.loghistory.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<HistoryItem>>>(Resource.Loading())
    val historyState: StateFlow<Resource<List<HistoryItem>>> = _historyState

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            try {
                val response = repository.getUserHistory(token)

                if (response.body()?.status != "success" || response.body()?.data == null) {
                    _historyState.value = Resource.Error(
                        "Gagal memuat riwayat"
                    )
                    return@launch
                }
                _historyState.value =
                    Resource.Success(response.body()?.data ?: emptyList())

            } catch (e: Exception) {
                e.printStackTrace()
                _historyState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan")
            }
        }
    }
}