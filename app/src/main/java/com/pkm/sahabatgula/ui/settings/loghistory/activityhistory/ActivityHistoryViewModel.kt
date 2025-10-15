package com.pkm.sahabatgula.ui.settings.loghistory.activityhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.convertUtcToLocalDateOnly
import com.pkm.sahabatgula.data.remote.model.ActivityLog
import com.pkm.sahabatgula.data.remote.model.FoodLog
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
                val responseList = repository.getUserHistory(token) ?: emptyList()

                val grouped = responseList.flatMap { item ->
                    val activityDates = item.activities.map { act ->
                        act to convertUtcToLocalDateOnly(act.time)
                    }
                    val foodDates = item.foods.map { food ->
                        food to convertUtcToLocalDateOnly(food.time)
                    }

                    val pairs = activityDates.map { Pair(it.second, listOf(it.first)) } +
                            foodDates.map { Pair(it.second, listOf(it.first)) }
                    pairs
                }.groupBy { it.first }
                    .map { (localDate, pairs) ->
                        val activities = pairs.flatMap { it.second }.filterIsInstance<ActivityLog>()
                        val foods = pairs.flatMap { it.second }.filterIsInstance<FoodLog>()

                        HistoryItem(
                            date = localDate,
                            foods = foods,
                            activities = activities
                        )
                    }
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
