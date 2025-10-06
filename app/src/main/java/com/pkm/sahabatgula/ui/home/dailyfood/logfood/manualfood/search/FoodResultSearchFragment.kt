package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentResultSearchBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.FoodPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodResultSearchFragment : Fragment() {
     private var _binding: FragmentResultSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodResultSearchViewModel by viewModels()
    private lateinit var foodAdapter: FoodPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeFoods()
    }

    private fun setupUI() {
        val query = viewModel.query
        val formattedText = getString(R.string.search_result_subtitle, query)
        binding.tvSubtitleResult.text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodPagingAdapter { foodItemManual ->
            val action = FoodResultSearchFragmentDirections.actionFoodResultSearchToDetailFoodFragment(
                foodItemManual = foodItemManual,
                foodItem = null
            )
            findNavController().navigate(action)
        }

        binding.rvSearchResult.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    private fun observeFoods() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.foods.collectLatest { pagingData ->
                    foodAdapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}