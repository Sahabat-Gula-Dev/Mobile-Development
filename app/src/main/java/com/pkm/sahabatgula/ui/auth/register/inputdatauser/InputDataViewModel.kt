package com.pkm.sahabatgula.ui.auth.register.inputdatauser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.ProfileData
import com.pkm.sahabatgula.data.remote.model.SetupProfileResponse
import com.pkm.sahabatgula.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputDataViewModel @Inject constructor(private val  profileRepository: ProfileRepository) : ViewModel(){

    private val _profileData = MutableStateFlow(ProfileData())
    val profileData: StateFlow<ProfileData> = _profileData

    val _setupResult = MutableStateFlow<Resource<SetupProfileResponse>?>(null)
    val setupResult: StateFlow<Resource<SetupProfileResponse>?> = _setupResult

    fun selectGender(gender: String) {
        _profileData.update { it.copy(gender = gender)}
    }

    fun selectAge(age: Int){
        _profileData.update { it.copy(age = age) }
        Log.d("DARI VIEW MODEL", "Usia dipilih: $age, gender ${_profileData.value.gender}")
    }

    fun selectHeight(height: Int){
        _profileData.update { it.copy(height = height) }
        Log.d("DARI VIEW MODEL", "Usia dipilih: ${_profileData.value.age}, gender: ${_profileData.value.gender}, tinggi: ${_profileData.value.height}")

    }

    fun submitProfileData() {
        viewModelScope.launch {
            _setupResult.value = Resource.Loading()
            _setupResult.value = profileRepository.setupProfile(_profileData.value)
        }
    }
}
