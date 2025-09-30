package com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.result

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.databinding.FragmentResultScanFoodBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.FoodScanViewModel
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.ScanUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.navigation.findNavController

@AndroidEntryPoint
class ResultScanFoodFragment : Fragment() {

    private var _binding: FragmentResultScanFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodScanViewModel by viewModels()
    private lateinit var resultScanAdapter: ResultScanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentResultScanFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DEBUG_NAV", "ResultScanFoodFragment: Berhasil dibuat dan ditampilkan (onViewCreated).")

        setupRecyclerView()

        val uri = arguments?.getString("uri")?.toUri()
        viewModel.predictImage(uri)
        observeUiState()

    }

    private fun setupRecyclerView() {
        resultScanAdapter = ResultScanAdapter { foodItem ->
            val action = ResultScanFoodFragmentDirections.actionResultFoodScanToDetailFoodFragment(foodItem)
            view?.findNavController()?.navigate(action)

        }
        binding.rvResult.apply {
            adapter = resultScanAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }


    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                binding.rvResult.isVisible = state is ScanUiState.Success

                when (state) {
                    is ScanUiState.Success -> {
                        if (state.foodItems.isEmpty()) {
                            // nanti ganti pake state
                            Toast.makeText(requireContext(), "Tidak ada makanan yang ditemukan", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("ResultScanFragment", "Mengirim data ke adapter. Jumlah: ${state.foodItems.size}")
                            resultScanAdapter.submitList(state.foodItems)
                        }
                    }
                    is ScanUiState.Error -> {
                        Log.e("ResultScanFragment", "Error: ${state.message}")
                        Toast.makeText(requireContext(), "Error : ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    is ScanUiState.Loading -> { /* Handled by visibility toggle */ }
                }
            }
        }
    }


}