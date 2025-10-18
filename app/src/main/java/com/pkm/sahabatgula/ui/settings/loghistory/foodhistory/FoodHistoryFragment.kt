package com.pkm.sahabatgula.ui.settings.loghistory.foodhistory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.core.utils.filterForFood
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentFoodHistoryBinding
import com.pkm.sahabatgula.ui.settings.loghistory.HistorySharedViewModel
import com.pkm.sahabatgula.ui.settings.loghistory.ParentFoodHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FoodHistoryFragment : Fragment() {

    private var _binding: FragmentFoodHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistorySharedViewModel by activityViewModels()
    private lateinit var parentAdapter: ParentFoodHistoryAdapter

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodHistoryBinding.inflate(inflater, container, false)
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

        parentAdapter = ParentFoodHistoryAdapter(emptyList())
        binding.rvParentFood.layoutManager = LinearLayoutManager(requireContext())
        binding.rvParentFood.adapter = parentAdapter
        viewModel.fetchHistory(token)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyState.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data?.filterForFood() ?: emptyList()

                            if (data.isEmpty()) {
                                binding.layoutEmpty.root.visibility = View.VISIBLE
                                binding.layoutEmpty.tvTitle.text = "Riwayat Makanan Kosong"
                                binding.layoutEmpty.tvMessage.text =
                                    "Gluby belum menemukan data makanan kamu. Yuk mulai catat konsumsi harianmu!"
                                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                                binding.rvParentFood.visibility = View.GONE
                                Log.d("FoodHistoryFragment", "Empty")
                            } else {
                                binding.layoutEmpty.root.visibility = View.GONE
                                binding.rvParentFood.visibility = View.VISIBLE
                                parentAdapter.updateData(data)
                            }
                        }

                        is Resource.Error -> {
                            binding.layoutEmpty.root.visibility = View.VISIBLE
                            binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                            binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                            binding.layoutEmpty.tvMessage.text =
                                "Gluby mengalami kendala saat ambil data makanan. Periksa koneksi atau muat ulang halaman."
                            binding.rvParentFood.visibility = View.GONE
                            Log.e("FoodHistoryFragment", "Error: ${resource.message}")
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
