package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.databinding.FragmentLogManualFoodBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.LogFoodFragment
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.LogFoodFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogManualFoodFragment : Fragment() {

    private var _binding: FragmentLogManualFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogManualFoodViewModel by viewModels()
    private lateinit var pagingAdapter: FoodPagingAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogManualFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.cardSuggestionFoodManuallyInput
        binding.cardSuggestionFoodManuallyInput.apply {
            icAction.setImageResource(R.drawable.ic_docs_add_log)
            tvTitleAction.text = "Gak Nemu Menu Yang Pas?"
            tvSubtitleAction.text = "Yuk, susun sendiri makanannya dari bahan yang tersedia"

            root.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_card_action)
            root.setOnClickListener {
                findNavController().navigate(R.id.action_add_log_food_to_log_manual_custom_food_fragment)
            }
        }

        Log.d("FragmentLifecycle", "LogManualFoodFragment: onViewCreated CALLED")

        setupRecyclerView()
        observeViewModel()
        setupSearch()
    }

    private fun setupRecyclerView() {
        pagingAdapter = FoodPagingAdapter { foodItemManual ->
//            val navController = requireParentFragment().findNavController()
            val action = LogFoodFragmentDirections.actionAddLogFoodToDetailFoodFragment(
                null, foodItemManual)
//            navController.navigate(action)
            findNavController().navigate(action)


        }
        binding.rvFood.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
            // biar ada loading tambah adapter untuk paginglaoding di sini
        }
    }

    private fun observeViewModel() {
        // Mengamati data makanan paginasi
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.foodPagingData.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

//         Mengamati dan membuat chips kategori secara dinamis
        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.chipGroupFoodCategories.removeAllViews() // Hapus chip statis
                    addCategoryChips(resource.data)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
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


}