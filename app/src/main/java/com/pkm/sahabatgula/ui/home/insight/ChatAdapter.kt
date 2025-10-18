package com.pkm.sahabatgula.ui.home.insight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.data.local.room.ChatMessageEntity
import com.pkm.sahabatgula.data.local.room.Sender
import com.pkm.sahabatgula.databinding.ItemChatGeminiBinding
import com.pkm.sahabatgula.databinding.ItemChatTypingBinding
import com.pkm.sahabatgula.databinding.ItemChatUserBinding

class ChatAdapter : ListAdapter<ChatMessageEntity, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_GEMINI = 2
        private const val VIEW_TYPE_TYPING = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.id == Int.MIN_VALUE && message.message == "TYPING_INDICATOR" -> VIEW_TYPE_TYPING
            message.sender == Sender.USER -> VIEW_TYPE_USER
            else -> VIEW_TYPE_GEMINI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemChatUserBinding.inflate(inflater, parent, false)
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_GEMINI -> {
                val binding = ItemChatGeminiBinding.inflate(inflater, parent, false)
                GeminiMessageViewHolder(binding)
            }
            VIEW_TYPE_TYPING -> {
                val binding = ItemChatTypingBinding.inflate(inflater, parent, false)
                TypingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is GeminiMessageViewHolder -> holder.bind(message)
            is TypingViewHolder -> holder.bind()
        }
    }

    class UserMessageViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessageEntity) {
            binding.tvMessage.text = chatMessage.message
        }
    }

    class GeminiMessageViewHolder(private val binding: ItemChatGeminiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessageEntity) {
            binding.tvMessage.text = chatMessage.message
        }
    }

    class TypingViewHolder(private val binding: ItemChatTypingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isAnimating = false

        fun bind() {
            if (!isAnimating) {
                isAnimating = true
                startTypingAnimation()
            }
        }

        private fun startTypingAnimation() {
            val baseText = "Gluby sedang mengetik"
            var dotCount = 0

            binding.tvTyping.post(object : Runnable {
                override fun run() {
                    dotCount = (dotCount + 1) % 4
                    val dots = ".".repeat(dotCount)
                    binding.tvTyping.text = "$baseText$dots"
                    binding.tvTyping.postDelayed(this, 500)
                }
            })
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessageEntity>() {
        override fun areItemsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
            return oldItem.id == newItem.id && oldItem.isTyping == newItem.isTyping
        }

        override fun areContentsTheSame(oldItem: ChatMessageEntity, newItem: ChatMessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}
