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
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentActivityHistoryBinding
import com.pkm.sahabatgula.ui.settings.loghistory.ParentActivityHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@Suppress("DEPRECATION")
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
                    is Resource.Success -> {
                        val data = resource.data
                            ?.filter { !it.activities.isNullOrEmpty() } // Pastikan filter juga di sini
                            ?: emptyList()

                        if (data.isEmpty()) {
                            binding.layoutEmpty.root.visibility = View.VISIBLE
                            binding.rvParentActivity.visibility = View.GONE
                        } else {
                            binding.layoutEmpty.root.visibility = View.GONE
                            binding.rvParentActivity.visibility = View.VISIBLE
                            binding.rvParentActivity.adapter = ParentActivityHistoryAdapter(data)
                        }
                    }

                    is Resource.Error -> {
                        binding.layoutEmpty.root.visibility = View.VISIBLE
                        binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                        binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                        binding.layoutEmpty.tvMessage.text = "Gluby mengalami kendala saat ambil data. Coba periksa koneksi atau muat ulang halaman"
                        binding.rvParentActivity.visibility = View.GONE
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}