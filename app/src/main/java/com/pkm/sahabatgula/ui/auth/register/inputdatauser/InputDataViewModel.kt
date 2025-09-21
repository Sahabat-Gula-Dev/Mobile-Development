package com.pkm.sahabatgula.ui.auth.register.inputdatauser

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
        _profileData.update { currentState ->
            currentState.copy(gender = gender)
        }
    }

    fun selectAge(age: Int){
        _profileData.update { it.copy(age = age) }
    }

    fun submitProfileData() {
        viewModelScope.launch {
            _setupResult.value = Resource.Loading()
            _setupResult.value = profileRepository.setupProfile(_profileData.value)
        }
    }
}
