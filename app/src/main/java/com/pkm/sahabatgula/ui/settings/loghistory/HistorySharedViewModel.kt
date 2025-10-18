package com.pkm.sahabatgula.ui.settings.loghistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.convertUtcToLocalDateOnly
import com.pkm.sahabatgula.data.remote.model.HistoryItem
import com.pkm.sahabatgula.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistorySharedViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<HistoryItem>>>(Resource.Loading())
    val historyState: StateFlow<Resource<List<HistoryItem>>> = _historyState

    private var isDataFetched = false

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            try {
                val responseList = repository.getUserHistory(token) ?: emptyList()

                val grouped = responseList.flatMap { item ->
                    // 1️⃣ Ambil semua tanggal unik dari foods & activities
                    val allDates = buildSet {
                        item.foods.forEach { add(convertUtcToLocalDateOnly(it.time)) }
                        item.activities.forEach { add(convertUtcToLocalDateOnly(it.time)) }
                    }

                    // 2️⃣ Buat HistoryItem per tanggal
                    allDates.map { date ->
                        val foodsForDate = item.foods.filter {
                            convertUtcToLocalDateOnly(it.time) == date
                        }
                        val activitiesForDate = item.activities.filter {
                            convertUtcToLocalDateOnly(it.time) == date
                        }

                        HistoryItem(
                            date = date,
                            foods = foodsForDate,
                            activities = activitiesForDate
                        )
                    }
                }
                    // 3️⃣ Kalau ada beberapa HistoryItem dengan tanggal sama, gabungkan
                    .groupBy { it.date }
                    .map { (date, items) ->
                        HistoryItem(
                            date = date,
                            foods = items.flatMap { it.foods },
                            activities = items.flatMap { it.activities }
                        )
                    }
                    // 4️⃣ Urutkan descending berdasarkan tanggal
                    .sortedByDescending { it.date }

                _historyState.value = Resource.Success(grouped)
            } catch (e: Exception) {
                e.printStackTrace()
                _historyState.value = Resource.Error(
                    e.localizedMessage ?: "Terjadi kesalahan saat memuat riwayat"
                )
            }
        }
    }

}