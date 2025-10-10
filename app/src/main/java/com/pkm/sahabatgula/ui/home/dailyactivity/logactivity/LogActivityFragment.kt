package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.databinding.FragmentLogActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.forEach

@AndroidEntryPoint
class LogActivityFragment : Fragment() {
    private var _binding: FragmentLogActivityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogActivityViewModel by viewModels()
    private lateinit var pagingAdapter: ActivityPagingAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogActivityBinding.inflate(inflater, container, false)
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
    
        pagingAdapter = ActivityPagingAdapter(
            onSelectClick = { activityItem ->
                viewModel.toggleActivitySelection(activityItem)
            },
            onExpandClick = { foodItem ->
                viewModel.onExpandClicked(foodItem)
            }
        )
    
        binding.rvActivity .apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupPagingDataObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activityPagingData.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }
    
    private fun setupChipsObserver() {
        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.chipGroupActivityCategories.removeAllViews() // Hapus chip statis
                    addCategoryChips(resource.data)
                }
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> { /* Tampilkan loading jika perlu */ }
            }
        }
    }
    
    private fun addCategoryChips(categories: List<ActivityCategories>?) {
        val chipGroup = binding.chipGroupActivityCategories
        chipGroup.removeAllViews()

        val customTypefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_regular)
        val customTypefaceBold = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_bold)

        val backgroundStates = arrayOf(
            intArrayOf(android.R.attr.state_checked), // Saat terpilih
            intArrayOf(-android.R.attr.state_checked) // Saat normal (tidak terpilih)
        )
        val backgroundColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_primary), // Warna solid saat terpilih
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)  // Warna putih/surface saat normal
        )
        val backgroundColorStateList = ColorStateList(backgroundStates, backgroundColors)

        // --- Aturan untuk Warna Teks ---
        val textStates = arrayOf(
            intArrayOf(android.R.attr.state_checked), // Saat terpilih
            intArrayOf(-android.R.attr.state_checked) // Saat normal
        )
        val textColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary), // Warna putih saat terpilih
            ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)   // Warna abu-abu saat normal
        )
        val textColorStateList = ColorStateList(textStates, textColors)

        // Fungsi kecil untuk membantu konversi DP ke Pixel
        fun Float.dpToPx(): Float = (this * resources.displayMetrics.density)

        // --- Buat Chip "Semua" ---
        val allChip = Chip(context).apply {
            text = "Semua"
            isCheckable = true
            isChecked = true
            id = View.generateViewId() // Atau View.NO_ID

            // Warna
            chipBackgroundColor = backgroundColorStateList
            setTextColor(textColorStateList)
            setChipStrokeColorResource(R.color.md_theme_outline) // Warna outline
            chipStrokeWidth = 1f.dpToPx() // Lebar outline 1dp

            // Bentuk (sangat rounded)
            chipCornerRadius = 50f.dpToPx()

            // Menghilangkan ikon centang saat terpilih
            isCheckedIconVisible = false
            typeface = customTypefaceBold
            textSize = 11f
            height = 36

        }
        chipGroup.addView(allChip)
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = true


        categories?.forEach { category ->
            val chip = Chip(context).apply {
                text = category.name
                textSize = 11f
                isCheckable = true
                id = View.generateViewId()
                tag = category.id
                height = 36

                // --- Terapkan Style yang sama ---
                chipBackgroundColor = backgroundColorStateList
                setTextColor(textColorStateList)
                setChipStrokeColorResource(R.color.md_theme_outline)
                chipStrokeWidth = 1f.dpToPx()
                chipCornerRadius = 50f.dpToPx()
                isCheckedIconVisible = false
//                setTextAppearance(R.style.MyChipTextAppearance)
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
        editText.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(300L)
                editable?.let {
                    val query = it.toString().trim()
                    viewModel.searchActivity(query.ifEmpty { null })
                }
            }
        }
    
        // 2. Listener untuk aksi keyboard "Search"
        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                viewModel.searchActivity(query.ifEmpty { null })
    
                // Sembunyikan keyboard
                textView.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    
        // Sembunyikan keyboard saat ikon 'x' diklik
        binding.searchBarActivity.setEndIconOnClickListener {
            editText.text?.clear()
            editText.hideKeyboard()
        }
    }
    
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    
    private fun setupButtonListener() {
        binding.btnLogThisActivity .setOnClickListener {
            viewModel.logSelectedActivities()
        }
    }
    
    private fun setupLogStatusObserver() {
        viewModel.logActivityStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Tampilkan loading indicator
    //                    binding.btnLogThisFood.isEnabled = false
    //                    binding.btnLogThisFood.text = "Mencatat..."
                }
                is Resource.Success -> {
                    binding.btnLogThisActivity.isEnabled = true
//                    binding.btnLogThisActivity.text = "Catat Aktivitas"
                    Toast.makeText(context, "Aktivitas berhasil dicatat!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnLogThisActivity.isEnabled = true
//                    binding.btnLogThisActivity.text = "Catat Aktivitas"
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
