package com.pkm.sahabatgula.ui.explore.event

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.EventCategory
import com.pkm.sahabatgula.databinding.FragmentExploreEventBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.getValue

@AndroidEntryPoint
class ExploreEventFragment : Fragment() {

    private var _binding: FragmentExploreEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreEventViewModel by viewModels()
    private lateinit var eventAdapter: EventPagingDataAdapter
    private var searchJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndNavigate()
        observeEvents()
        observeCategories()
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        setupChipGroup(resource.data)
                    }
                    is Resource.Error -> {
                        // Tampilkan pesan error jika gagal memuat kategori
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> { /* Tampilkan loading jika perlu */ }
                }
            }
        }
    }

    private fun setupChipGroup(categories: List<EventCategory>?) {
        val chipGroup = binding.chipGroupEventCategories
        chipGroup.removeAllViews()

        val customTypefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_regular)
        val customTypefaceBold = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_bold)

        val backgroundStates = arrayOf(
            intArrayOf(android.R.attr.state_checked), // Saat terpilih
            intArrayOf(-android.R.attr.state_checked) // Saat normal (tidak terpilih)
        )
        val backgroundColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_primary), // Warna solid saat terpilih
            ContextCompat.getColor(requireContext(), R.color.md_theme_surface)  // Warna putih/surface saat normal
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

        // Atur listener
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.selectCategory(null)
                return@setOnCheckedStateChangeListener
            }

            val selectedId = checkedIds.firstOrNull()

            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.typeface = if (chip.id == selectedId) customTypefaceBold else customTypefaceRegular
            }

            if (selectedId != null) {
                val selectedChip = group.findViewById<Chip>(selectedId)
                val categoryId = selectedChip?.tag as? Int  // ← Ambil ID dari tag, bukan dari ID view
                viewModel.selectCategory(categoryId)
            }
        }

    }

    private fun setupSearchAndNavigate() {
        binding.searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                val categoryIdString = viewModel.selectedCategoryId.value?.toString() ?: ""

                val bundle = Bundle().apply {
                    putString("searchQuery", query)
                    putString("categoryId", categoryIdString)
                }

                findNavController().navigate(
                    R.id.action_explore_event_to_result_search_event,
                    bundle
                )
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventPagingDataAdapter { event ->

            val action = ExploreEventFragmentDirections.actionExploreEventToDetailEvent (event)
            findNavController().navigate(action)
        }
        binding.rvListEvents.apply { // Ganti dengan ID RecyclerView dari XML Anda
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Bungkus collector di dalam repeatOnLifecycle
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Ganti .collect menjadi .collectLatest
                viewModel.events.collectLatest { pagingData ->
                    Log.d("SearchDebug", "Mencoba submit PagingData baru ke adapter...")
                    eventAdapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }


}