package com.pkm.sahabatgula.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Enum untuk membedakan pengirim, ini lebih aman daripada menggunakan String
enum class Sender {
    USER, GEMINI
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val message: String,

    val sender: Sender,
    val timestamp: Long,
    val isError: Boolean = false
)