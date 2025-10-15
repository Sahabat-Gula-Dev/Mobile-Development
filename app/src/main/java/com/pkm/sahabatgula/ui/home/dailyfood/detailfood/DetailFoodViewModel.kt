package com.pkm.sahabatgula.ui.home.dailyfood.detailfood

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.room.ProfileDao
import com.pkm.sahabatgula.data.local.room.ProfileEntity
import com.pkm.sahabatgula.data.remote.model.Food
import com.pkm.sahabatgula.data.remote.model.FoodItemRequest
import com.pkm.sahabatgula.data.repository.LogFoodRepository
import com.pkm.sahabatgula.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailFoodViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val logFoodRepository: LogFoodRepository,
    private val profileDao: ProfileDao
    ): ViewModel() {

    private val _foodDetail = MutableLiveData<Resource<Food>>()
    val foodDetail: MutableLiveData<Resource<Food>> = _foodDetail

    // for log food
    private val _logFoodStatus = MutableLiveData<Resource<Unit>>()
    val logFoodStatus: LiveData<Resource<Unit>> = _logFoodStatus

    private val _profile = MutableStateFlow<ProfileEntity?>(null)
    val profile: StateFlow<ProfileEntity?> get() = _profile

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = profileDao.getProfile()
        }
    }

    fun fetchFoodDetail(id: String?) {
        if (id == null) {
            _foodDetail.value = Resource.Error("Food ID is missing.")
            return
        }
        viewModelScope.launch {
            _foodDetail.value = Resource.Loading()
            try {
                val response = scanRepository.getFoodDetail(id)
                _foodDetail.value = response
            } catch (e: Exception) {
                _foodDetail.value = Resource.Error(e.message ?: "An unknown error occurred")
                Log.e("DetailFoodViewModel", "Error fetching food detail", e)
            }
        }
    }

    fun logThisFood(foodId: String, portion: Int) {
        viewModelScope.launch {
            _logFoodStatus.value = Resource.Loading()

            val foodItem = FoodItemRequest(foodId = foodId, portion = portion)
            val requestItems = listOf(foodItem)

            val result = logFoodRepository.logFood(requestItems)
            _logFoodStatus.value = result
        }
    }

}