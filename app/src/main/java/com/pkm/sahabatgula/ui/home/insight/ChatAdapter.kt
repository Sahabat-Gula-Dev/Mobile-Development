package com.pkm.sahabatgula.ui.home.insight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.data.local.room.ChatMessageEntity
import com.pkm.sahabatgula.data.local.room.Sender
import com.pkm.sahabatgula.databinding.ItemChatGeminiBinding
import com.pkm.sahabatgula.databinding.ItemChatUserBinding

class ChatAdapter : ListAdapter<ChatMessageEntity, RecyclerView.ViewHolder>(DiffCallback()) {

    // Definisikan konstanta untuk setiap tipe view
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_GEMINI = 2
    }

    /**
     * Fungsi ini menentukan tipe view (layout) mana yang harus digunakan
     * berdasarkan pengirim pesan.
     */
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.sender == Sender.USER) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_GEMINI
        }
    }

    /**
     * Fungsi ini membuat ViewHolder yang sesuai berdasarkan viewType.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemChatUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_GEMINI -> {
                val binding = ItemChatGeminiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GeminiMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    /**
     * Fungsi ini menghubungkan data dari ChatMessageEntity ke ViewHolder yang benar.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is GeminiMessageViewHolder -> holder.bind(message)
        }
    }

    // --- ViewHolder untuk pesan pengguna ---
    class UserMessageViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessageEntity) {
            binding.tvMessage.text = chatMessage.message
        }
    }

    // --- ViewHolder untuk pesan Gemini ---
    class GeminiMessageViewHolder(private val binding: ItemChatGeminiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessageEntity) {
            binding.tvMessage.text = chatMessage.message
            // (Opsional) Tampilkan indikator error jika pesan gagal dimuat
            // if (chatMessage.isError) { ... }
        }
    }

    // --- DiffUtil untuk efisiensi RecyclerView ---
    class DiffCallback : DiffUtil.ItemCallback<ChatMessageEntity>() {
        override fun areItemsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}