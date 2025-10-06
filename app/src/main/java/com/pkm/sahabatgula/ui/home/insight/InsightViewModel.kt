package com.pkm.sahabatgula.ui.home.insight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.local.room.ChatMessageEntity
import com.pkm.sahabatgula.data.repository.InsightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightChatViewModel @Inject constructor(
    private val insightRepository: InsightRepository
) : ViewModel() {

    // 1. KELOLA RIWAYAT CHAT
    // Mengambil Flow dari repository dan mengubahnya menjadi StateFlow yang bisa diamati oleh UI.
    // UI akan otomatis update setiap kali ada pesan baru di database.
    val chatHistory: StateFlow<List<ChatMessageEntity>> = insightRepository.getChatHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Mulai mengamati saat UI terlihat
            initialValue = emptyList() // Nilai awal saat belum ada data
        )

    // (Opsional tapi sangat direkomendasikan) State untuk menunjukkan proses loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 2. FUNGSI INTI UNTUK MENGIRIM PESAN
    fun sendMessage(question: String) {
        // Jangan kirim pesan jika kosong
        if (question.isBlank()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Mulai loading
            try {
                // ViewModel hanya perlu memanggil satu fungsi di repository.
                // Repository akan menangani semua langkah: menyimpan pertanyaan, membuat prompt,
                // memanggil Gemini, dan menyimpan jawaban.
                insightRepository.askGemini(question)
            } finally {
                _isLoading.value = false // Selesai loading (baik sukses maupun gagal)
            }
        }
    }
}