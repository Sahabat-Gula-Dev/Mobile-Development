package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.search

import android.os.Bundle
import android.util.Log
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

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

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
        binding.tvHeaderResult.visibility = View.GONE
        binding.tvSubtitleResult.visibility = View.GONE

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

        foodAdapter.addLoadStateListener { loadStates ->
            val isListEmpty = foodAdapter.itemCount == 0 &&
                    loadStates.refresh is androidx.paging.LoadState.NotLoading

            if (isListEmpty) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                binding.layoutEmpty.tvTitle.text = "Tidak Ada Hasil"
                binding.layoutEmpty.tvMessage.text = "Kami tidak menemukan apapun untuk pencarian ini. Coba gunakan kata kunci lain."
                binding.rvSearchResult.visibility = View.GONE
                binding.tvHeaderResult.visibility = View.GONE
                binding.tvSubtitleResult.visibility = View.GONE
                binding.topAppBar.setNavigationIcon(R.drawable.ic_close)
            } else {
                binding.layoutEmpty.root.visibility = View.GONE
                binding.rvSearchResult.visibility = View.VISIBLE
                binding.tvHeaderResult.visibility = View.VISIBLE
                binding.tvSubtitleResult.visibility = View.VISIBLE
                binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
            }

            // Kalau error saat load
            val errorState = loadStates.refresh as? androidx.paging.LoadState.Error
            if (errorState != null) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                binding.layoutEmpty.tvMessage.text = errorState.error.localizedMessage
                binding.rvSearchResult.visibility = View.GONE
                binding.tvHeaderResult.visibility = View.GONE
                binding.tvSubtitleResult.visibility = View.GONE
            }
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