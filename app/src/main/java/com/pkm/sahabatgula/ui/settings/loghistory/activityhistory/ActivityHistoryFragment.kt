package com.pkm.sahabatgula.ui.settings.loghistory.activityhistory

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentActivityHistoryBinding
import com.pkm.sahabatgula.ui.settings.loghistory.ParentActivityHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class ActivityHistoryFragment : Fragment() {
    private var _binding: FragmentActivityHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActivityHistoryViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) return

        binding.rvParentActivity.layoutManager = LinearLayoutManager(requireContext())
        viewModel.fetchHistory(token)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        binding.rvParentActivity.adapter = ParentActivityHistoryAdapter(resource.data)
                    }

                    is Resource.Error -> {
                        Log.e("DEBUG_NAV", "ActivityHistoryFragment: ${resource.message}")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}