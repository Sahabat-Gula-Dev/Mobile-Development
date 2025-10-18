package com.pkm.sahabatgula.ui.settings.loghistory.activityhistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.filterForActivity
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentActivityHistoryBinding
import com.pkm.sahabatgula.ui.settings.loghistory.HistorySharedViewModel
import com.pkm.sahabatgula.ui.settings.loghistory.ParentActivityHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class ActivityHistoryFragment : Fragment() {
    private var _binding: FragmentActivityHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistorySharedViewModel by activityViewModels()

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyState.collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data?.filterForActivity() ?: emptyList()

                            if (data.isEmpty()) {
                                binding.layoutEmpty.root.visibility = View.VISIBLE
                                binding.rvParentActivity.visibility = View.GONE
                                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                                binding.layoutEmpty.tvTitle.text = "Riwayat Aktivitas Kosong"
                                binding.layoutEmpty.tvMessage.text =
                                    "Catat aktivitas harianmu sekarang, Gluby siap bantu pantau progres kesehatanmu!"
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
