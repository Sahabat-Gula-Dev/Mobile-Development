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

    private lateinit var parentAdapter: ParentActivityHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) return

        parentAdapter = ParentActivityHistoryAdapter(emptyList())
        binding.rvParentActivity.layoutManager = LinearLayoutManager(requireContext())
        binding.rvParentActivity.adapter = parentAdapter

        viewModel.fetchHistory(token)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val data = resource.data
                            ?.filter { !it.activities.isNullOrEmpty() } ?: emptyList()

                        if (data.isEmpty()) {
                            binding.layoutEmpty.root.visibility = View.VISIBLE
                            binding.rvParentActivity.visibility = View.GONE
                            binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                            binding.layoutEmpty.tvTitle.text = "Belum Ada Aktivitas"
                            binding.layoutEmpty.tvMessage.text =
                                "Aktivitas yang kamu catat akan muncul di sini"
                        } else {
                            binding.layoutEmpty.root.visibility = View.GONE
                            binding.rvParentActivity.visibility = View.VISIBLE
                            parentAdapter.updateData(data)
                        }
                    }

                    is Resource.Error -> {
                        binding.layoutEmpty.root.visibility = View.VISIBLE
                        binding.rvParentActivity.visibility = View.GONE
                        binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                        binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                        binding.layoutEmpty.tvMessage.text =
                            "Glubby mengalami kendala saat ambil data. Coba periksa koneksi atau muat ulang halaman"
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
