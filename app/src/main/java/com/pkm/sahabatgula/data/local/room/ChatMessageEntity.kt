package com.pkm.sahabatgula.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Sender {
    USER, GEMINI
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val message: String,

    val sender: Sender,
    val timestamp: Long,
    val isError: Boolean = false,
    val isTyping: Boolean = false
)