package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.uriToFile
import com.pkm.sahabatgula.data.remote.model.FoodsItem
import com.pkm.sahabatgula.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScanUiState{
    data object Loading: ScanUiState
    data class Success(val foodItems: List<FoodsItem?>): ScanUiState
    data class Error(val message: String): ScanUiState
}

@HiltViewModel
class FoodScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Loading)
    val uiState: StateFlow<ScanUiState> = _uiState

    private val CONFIDENCE_THRESHOLD = 0.7

    fun predictImage(imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Loading

            val imageFile = uriToFile(imageUri, context)
            if (imageFile == null) {
                _uiState.value = ScanUiState.Error("Image file is null")
                return@launch
            }
            try {
                when (val result = scanRepository.predictImage(imageFile)) {
                    is Resource.Success -> {
                        val predictionData = result.data?.data
                        val confidence = predictionData?.predictionConfidence ?: 0.0

                        Log.d("FoodScanViewModel", "Confidence prediction: $confidence")

                        if (confidence >= CONFIDENCE_THRESHOLD) {
                            val foodList = predictionData?.foods ?: emptyList()
                            _uiState.value = ScanUiState.Success(foodList)
                            Log.d("FoodScanViewModel", "Prediksi valid, jumlah food items: ${foodList.size}")
                        } else {
                            _uiState.value = ScanUiState.Error(
                                "Confidence terlalu rendah (${String.format("%.2f", confidence)}). Minimal $CONFIDENCE_THRESHOLD"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = ScanUiState.Error(result.message ?: "Error")
                    }
                    else -> {}
                }
            } finally {
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        Log.d("FileCleanup", "File temporer berhasil dihapus: ${imageFile.path}")
                    } else {
                        Log.e("FileCleanup", "Gagal menghapus file temporer: ${imageFile.path}")
                    }
                }
            }
        }
    }
}