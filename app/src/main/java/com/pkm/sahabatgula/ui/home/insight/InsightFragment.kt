package com.pkm.sahabatgula.ui.home.insight

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentHomeBinding
import com.pkm.sahabatgula.databinding.FragmentInsightBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InsightFragment : Fragment() {

    private var _binding: FragmentInsightBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InsightChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeChatHistory()
        observeLoadingState()

        binding.btnSendChat.setOnClickListener {
            val question = binding.etChatInput.text.toString()
            viewModel.sendMessage(question)
            binding.etChatInput.text?.clear()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter() // Inisialisasi adapter
        binding.rvChatMessages.adapter = chatAdapter
        // (Opsional) Untuk membuat chat dimulai dari bawah
        val layoutManager = binding.rvChatMessages.layoutManager as LinearLayoutManager
        layoutManager.stackFromEnd = true
    }

    private fun observeChatHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatHistory.collect { messages ->
                    chatAdapter.submitList(messages)
                    // Auto-scroll ke pesan terbaru
                    if (messages.isNotEmpty()) {
                        binding.rvChatMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    // Tampilkan ProgressBar atau nonaktifkan tombol kirim saat loading
                    binding.btnSendChat.isEnabled = !isLoading
                }
            }
        }
    }

}