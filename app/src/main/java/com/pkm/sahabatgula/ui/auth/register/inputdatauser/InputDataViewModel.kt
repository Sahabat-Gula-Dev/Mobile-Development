package com.pkm.sahabatgula.ui.auth.register.inputdatauser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.SessionManager
import com.pkm.sahabatgula.data.local.room.ProfileDao
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
class InputDataViewModel @Inject constructor(private val  profileRepository: ProfileRepository, private val sessionManager: SessionManager, private val profileDao: ProfileDao) : ViewModel(){

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

    fun selectWeight(weight: Int){
        _profileData.update { it.copy(weight = weight) }
        Log.d("DARI VIEW MODEL",
            "Usia dipilih: ${_profileData.value.age}, " +
                    "gender: ${_profileData.value.gender}, " +
                    "tinggi: ${_profileData.value.height}, " +
                    "berat: ${_profileData.value.weight}")
    }

    fun selectWaistCirc(waistCirc: Int){
        _profileData.update { it.copy(waistCircumference = waistCirc) }
        Log.d("DARI VIEW MODEL",
            "Usia dipilih: ${_profileData.value.age}, " +
                    "gender: ${_profileData.value.gender}, " +
                    "tinggi: ${_profileData.value.height}, " +
                    "berat: ${_profileData.value.weight}, " +
                    "lingkar pinggang: ${_profileData.value.waistCircumference}")

    }

    fun selectBloodPressure(bloodPressure: Boolean){
        _profileData.update{it.copy(bloodPressure = bloodPressure)}
        Log.d("DARI VIEW MODEL",
            "Usia dipilih: ${_profileData.value.age}, " +
                    "gender: ${_profileData.value.gender}, " +
                    "tinggi: ${_profileData.value.height}, " +
                    "berat: ${_profileData.value.weight}, " +
                    "lingkar pinggang: ${_profileData.value.waistCircumference}, " +
                    "riwayat tekanan darah: ${_profileData.value.bloodPressure}")
    }

    fun selectHighBloodGlucose(highBloodGlucose: Boolean){
        _profileData.update{it.copy(bloodSugar = highBloodGlucose)}
        Log.d("DARI VIEW MODEL",
            "Usia dipilih: ${_profileData.value.age}, " +
                    "gender: ${_profileData.value.gender}, " +
                    "tinggi: ${_profileData.value.height}, " +
                    "berat: ${_profileData.value.weight}, " +
                    "lingkar pinggang: ${_profileData.value.waistCircumference}, " +
                    "riwayat tekanan darah: ${_profileData.value.bloodPressure}, " +
                    "riwayat gula darah: ${_profileData.value.bloodSugar}")

    }

    fun selectDailyConsumption(dailyConsumption: Boolean){
        _profileData.update{it.copy(eatVegetables = dailyConsumption)}
            Log.d("DARI VIEW MODEL",
                "Usia dipilih: ${_profileData.value.age}," +
                    "gender: ${_profileData.value.gender},"+
                    "tinggi: ${_profileData.value.height},"+
                    "berat: ${_profileData.value.weight},"+
                    "lingkar pinggang: ${_profileData.value.waistCircumference},"+
                    "riwayat tekanan darah: ${_profileData.value.bloodPressure},"+
                    "riwayat gula darah: ${_profileData.value.bloodSugar},"+
                    "konsumsi makanan: ${_profileData.value.eatVegetables}")

    }

    fun selectDiabetesFamily(diabetesFamily: String) {
        _profileData.update { it.copy(diabetesFamily = diabetesFamily)}

        Log.d("DARI VIEW MODEL",
            "Usia dipilih: ${_profileData.value.age}," +
                    "gender: ${_profileData.value.gender},"+
                    "tinggi: ${_profileData.value.height},"+
                    "berat: ${_profileData.value.weight},"+
                    "lingkar pinggang: ${_profileData.value.waistCircumference},"+
                    "riwayat tekanan darah: ${_profileData.value.bloodPressure},"+
                    "riwayat gula darah: ${_profileData.value.bloodSugar},"+
                    "konsumsi makanan: ${_profileData.value.eatVegetables},"+
                    "keluarga diabetes: ${_profileData.value.diabetesFamily}")

    }

    fun selectActivityLevel(activityLevel: String) {
        _profileData.update { it.copy(activityLevel = activityLevel) }
        Log.d("DARI VIEW MODEL",
                "Usia dipilih: ${_profileData.value.age}," +
                "gender: ${_profileData.value.gender},"+
                "tinggi: ${_profileData.value.height},"+
                "berat: ${_profileData.value.weight},"+
                "lingkar pinggang: ${_profileData.value.waistCircumference},"+
                "riwayat tekanan darah: ${_profileData.value.bloodPressure},"+
                "riwayat gula darah: ${_profileData.value.bloodSugar},"+
                "konsumsi makanan: ${_profileData.value.eatVegetables},"+
                "keluarga diabetes: ${_profileData.value.diabetesFamily},"+
                "tingkat aktivitas: ${_profileData.value.activityLevel}")

    }

    fun submitProfileData() {
        viewModelScope.launch {
            Log.d("PROFILE_SETUP", "Submit profile data dipanggil")
            _setupResult.value = Resource.Loading()

            val result = profileRepository.setupProfile(_profileData.value)
            _setupResult.value = result
            if (_setupResult.value is Resource.Success) {
                sessionManager.setProfileCompleted(true)
            }

            when (result) {
                is Resource.Success -> {
                    // Profil sudah sukses setup → fetch ulang profil untuk isi Room
                    profileRepository.fetchMyProfileAndCache()
                    Log.d("PROFILE_SETUP", "Profil berhasil disimpan di server dan lokal")
                }
                is Resource.Error -> {
                    Log.e("PROFILE_SETUP", "Gagal setup profil: ${result.message}")
                }
                else -> {}
            }

        }
    }



}
