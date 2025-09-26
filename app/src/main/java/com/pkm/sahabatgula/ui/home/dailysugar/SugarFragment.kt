package com.pkm.sahabatgula.ui.home.dailysugar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.pkm.sahabatgula.databinding.FragmentSugarBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SugarFragment : Fragment() {

    private var _binding: FragmentSugarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SugarViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSugarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sugarState.collect { state ->
                when (state) {
                    is SugarState.Success -> {
                        binding.piDailySugar.tvRemaining.text = state.remainingSugar.toInt().toString()
                        Log.d("SugarFragment", "Remaining Sugar: ${state.remainingSugar}")
                        // berapa max sugar
                        Log.d("SugarFragment", "Max Sugar: ${state.maxSugar}")
                        // berapa sugar saat ini
                        Log.d("SugarFragment", "Current Sugar: ${state.currentSugar}")
                    }
                    else -> {}
                }
            }
        }
    }
}