package com.pkm.sahabatgula.ui.settings.loghistory.foodhistory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.local.TokenManager
import com.pkm.sahabatgula.databinding.FragmentFoodHistoryBinding
import com.pkm.sahabatgula.ui.settings.loghistory.ParentActivityHistoryAdapter
import com.pkm.sahabatgula.ui.settings.loghistory.ParentFoodHistoryAdapter
import com.pkm.sahabatgula.ui.settings.loghistory.activityhistory.FoodHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodHistoryFragment : Fragment() {

    private var _binding: FragmentFoodHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodHistoryViewModel by viewModels()
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

        binding.rvParentFood.layoutManager = LinearLayoutManager(requireContext())
        viewModel.fetchHistory(token)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.historyState.collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        val data = resource.data
                            ?.filter { !it.activities.isNullOrEmpty() } ?: emptyList()


                        if (data.isEmpty()) {
                            binding.layoutEmpty.root.visibility = View.VISIBLE
                            binding.layoutEmpty.tvTitle.text = "Riwayat Makanan Kosong"
                            binding.layoutEmpty.tvMessage.text = "Gluby belum menemukan data makanan kamu. Yuk mulai catat konsumsi harianmu!"
                            binding.rvParentFood.visibility = View.GONE
                            Log.d("FoodHistoryFragment", "Empty")
                        } else {
                            binding.layoutEmpty.root.visibility = View.GONE
                            binding.rvParentFood.visibility = View.VISIBLE
                            binding.rvParentFood.adapter = ParentFoodHistoryAdapter(data)
                            parentAdapter.updateData(data)
                        }
                    }

                    is Resource.Error -> {
                        binding.layoutEmpty.root.visibility = View.VISIBLE
                        binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                        binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                        binding.layoutEmpty.tvMessage.text = "Gluby mengalami kendala saat ambil data makanan. Periksa koneksi atau muat ulang halaman."
                        binding.rvParentFood.visibility = View.GONE
                        Log.e("FoodHistoryFragment", "Error: ${resource.message}")
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
