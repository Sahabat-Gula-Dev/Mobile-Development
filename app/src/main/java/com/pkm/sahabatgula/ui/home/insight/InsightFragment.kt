package com.pkm.sahabatgula.ui.home.insight

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
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


        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.layoutEmpty.root.visibility = View.GONE

        setupRecyclerView()
        observeChatHistory()
        observeLoadingState()

        binding.btnSendChat.setOnClickListener {
            val question = binding.etChatInput.text.toString()
            viewModel.sendMessage(question)
            binding.etChatInput.text?.clear()
        }

        binding.toolbar.title
        // ubah title jadi bold
        binding.toolbar.title = "Gluby - Asisten Kesehatan"


        binding.etChatInput.setOnFocusChangeListener { view, hasFocus ->
            val editText = view as com.google.android.material.textfield.TextInputEditText
            if (hasFocus) {
                editText.hint = ""
            } else {
                editText.hint = "Tanya sesuatu..."
            }
        }

    }


    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.adapter = chatAdapter
        val layoutManager = binding.rvChatMessages.layoutManager as LinearLayoutManager
        layoutManager.stackFromEnd = true
    }

    private fun observeChatHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatHistory.collect { messages ->
                    chatAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.rvChatMessages.smoothScrollToPosition(messages.size - 1)
                        binding.layoutEmpty.root.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.root.visibility = View.VISIBLE
                        binding.layoutEmpty.tvTitle.text = "Belum ada pertanyaan"
                        binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_email)
                        binding.layoutEmpty.tvMessage.text = "Kamu belum menanyakan apa pun. Glubby siap bantu jawab pertanyaan seputar kesehatanmu!"
                    }
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.btnSendChat.isEnabled = !isLoading
                }
            }
        }
    }

}