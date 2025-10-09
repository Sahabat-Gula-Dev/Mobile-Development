package com.pkm.sahabatgula.ui.home.insight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkm.sahabatgula.data.local.room.ChatMessageEntity
import com.pkm.sahabatgula.data.local.room.Sender
import com.pkm.sahabatgula.data.repository.InsightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightChatViewModel @Inject constructor(
    private val insightRepository: InsightRepository
) : ViewModel() {

    private val chatHistoryFlow = insightRepository.getChatHistory()
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> get() = _isTyping
    val chatHistory: StateFlow<List<ChatMessageEntity>> = combine(
        chatHistoryFlow,
        _isTyping
    ) { messages, typing ->
        if (typing) {

            val lastUserIndex = messages.indexOfLast { it.sender == Sender.USER }
            val typingMessage = ChatMessageEntity(
                id = Int.MIN_VALUE,
                message = "TYPING_INDICATOR",
                sender = Sender.GEMINI,
                timestamp = System.currentTimeMillis()
            )

            val newList = messages.toMutableList()
            if (lastUserIndex != -1 && lastUserIndex < newList.size) {
                newList.add(lastUserIndex + 1, typingMessage)
            } else {
                newList.add(typingMessage)
            }
            newList
        } else {
            messages
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _isTyping.value = true
            try {
                insightRepository.askGemini(question)
            } finally {
                delay(0)
                _isTyping.value = false
                _isLoading.value = false
            }
        }
    }
}
