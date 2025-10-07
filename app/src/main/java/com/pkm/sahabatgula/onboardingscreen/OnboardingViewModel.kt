package com.pkm.sahabatgula.onboardingscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository
): ViewModel(){
    private val _items = MutableLiveData<List<OnboardingItem>>()
    val items: LiveData<List<OnboardingItem>> get() = _items

    private val _isFirstTime = MutableLiveData<Boolean>()
    val isFirstTime: LiveData<Boolean> get() = _isFirstTime

    init {
        _items.value = listOf(
            OnboardingItem(
                R.drawable.onboarding_1,
                "Cek Asupan Harian",
                "Lihat berapa banyak kalori, gula, lemak, karbo, dan protein yang kamu konsumsi tiap hari"
            ),
            OnboardingItem(
                R.drawable.onboarding_2,
                "Scan Makanan",
                "Foto makanan kamu, biar AI kasih tahu nilai gizinya dalam hitungan detik"
            ),
            OnboardingItem(
                R.drawable.onboarding_3,
                "Edukasi Kesehatan",
                "Cari artikel, event, dan berita kesehatan yang menarik dan bermanfaat"
            )
        )

        // cek apakah ini first time
        _isFirstTime.value = repository.isFirstTime()
    }

    fun completeOnboarding() {
        repository.setFirstTime(false)
        _isFirstTime.value = false
    }
}