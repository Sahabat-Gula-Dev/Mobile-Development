package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.databinding.FragmentLogManualCustomFoodBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood.CustomFoodPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogManualCustomFoodFragment : Fragment() {


    private var _binding: FragmentLogManualCustomFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogManualCustomFoodViewModel by viewModels()
    private lateinit var pagingAdapter: CustomFoodPagingAdapter
    private var searchJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogManualCustomFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupChipsObserver()
        setupPagingDataObserver()
        setupButtonListener()
        setupLogStatusObserver()
    }

    private fun setupRecyclerView() {

        pagingAdapter = CustomFoodPagingAdapter(
            onSelectClick = { foodItem ->
                viewModel.toggleFoodSelection(foodItem)
            },
            onExpandClick = { foodItem ->
                viewModel.onExpandClicked(foodItem)
            }
        )

        binding.rvCustomFood.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupPagingDataObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.foodPagingData.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupChipsObserver() {
        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.chipGroupFoodCategories.removeAllViews() // Hapus chip statis
                    addCategoryChips(resource.data)
                }
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { /* Tampilkan loading jika perlu */ }
            }
        }
    }

    private fun addCategoryChips(categories: List<FoodCategories>?) {
        val chipGroup = binding.chipGroupFoodCategories
        val allChip = Chip(context).apply {
            text = "Semua"
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
        }
        chipGroup.addView(allChip)

        categories?.forEach { category ->
            val chip = Chip(context).apply {
                text = category.name
                isCheckable = true
                id = View.generateViewId()
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds[0])
                if (selectedChip.text == "Semua") {
                    viewModel.setCategory(null)
                } else {
                    val selectedCategory = categories?.find { it.name == selectedChip.text }
                    viewModel.setCategory(selectedCategory?.id)
                }
            }
        }
    }

    private fun setupSearch() {
        val editText = binding.searchEditText

        // 1. Listener untuk setiap kali teks berubah (termasuk saat dihapus oleh ikon 'x')
        editText.addTextChangedListener { editable ->
            // Batalkan job pencarian sebelumnya agar tidak menumpuk
            searchJob?.cancel()
            // Buat job baru dengan delay (debounce)
            searchJob = MainScope().launch {
                delay(300L) // Tunggu 300ms setelah user berhenti mengetik
                editable?.let {
                    val query = it.toString().trim()
                    // Kirim null jika query kosong, ini akan me-reset list
                    viewModel.searchFood(query.ifEmpty { null })
                }
            }
        }

        // 2. Listener untuk aksi keyboard "Search"
        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                viewModel.searchFood(query.ifEmpty { null })

                // Sembunyikan keyboard
                textView.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // Sembunyikan keyboard saat ikon 'x' diklik
        binding.searchBarFood.setEndIconOnClickListener {
            editText.text?.clear()
            editText.hideKeyboard()
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setupButtonListener() {
        binding.btnLogThisFood.setOnClickListener {
            viewModel.logSelectedFoods()
        }
    }

    private fun setupLogStatusObserver() {
        viewModel.logFoodStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Tampilkan loading indicator
//                    binding.btnLogThisFood.isEnabled = false
//                    binding.btnLogThisFood.text = "Mencatat..."
                }
                is Resource.Success -> {
                    binding.btnLogThisFood.isEnabled = true
                    binding.btnLogThisFood.text = "Catat Aktivitas"
                    Toast.makeText(context, "Makanan berhasil dicatat!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnLogThisFood.isEnabled = true
                    binding.btnLogThisFood.text = "Catat Aktivitas"
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}