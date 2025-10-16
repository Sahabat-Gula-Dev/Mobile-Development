@file:Suppress("DEPRECATION")

package com.pkm.sahabatgula.ui.explore.event

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.EventCategory
import com.pkm.sahabatgula.databinding.FragmentExploreEventBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        setupKeyboardListener()
        setupRecyclerView()
        setupSearchAndNavigate()
        observeEvents()
        observeCategories()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKeyboardListener() {
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            if (imeVisible) {
                navView.visibility = View.GONE
            }

            binding.rvListEvents.setPadding(
                binding.rvListEvents.paddingLeft,
                binding.rvListEvents.paddingTop,
                binding.rvListEvents.paddingRight,
                if (imeVisible) imeHeight else 0
            )

            insets
        }

        binding.root.setOnClickListener {
            if (binding.searchEditText.hasFocus()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
            }
        }

        binding.rvListEvents.setOnTouchListener { _, _ ->
            if (binding.searchEditText.hasFocus()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
            }
            false
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        setupChipGroup(resource.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Terjadi kesalahan saat memuat data kategori", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {  }
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
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val backgroundColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        val backgroundColorStateList = ColorStateList(backgroundStates, backgroundColors)

        val textStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val textColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary),
            ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
        )
        val textColorStateList = ColorStateList(textStates, textColors)

        fun Float.dpToPx(): Float = (this * resources.displayMetrics.density)

        val allChip = Chip(context).apply {
            text = "Semua"
            isCheckable = true
            isChecked = true
            id = View.generateViewId()

            chipBackgroundColor = backgroundColorStateList
            setTextColor(textColorStateList)
            setChipStrokeColorResource(R.color.md_theme_outline)
            chipStrokeWidth = 1f.dpToPx()
            chipCornerRadius = 50f.dpToPx()
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

                chipBackgroundColor = backgroundColorStateList
                setTextColor(textColorStateList)
                setChipStrokeColorResource(R.color.md_theme_outline)
                chipStrokeWidth = 1f.dpToPx()
                chipCornerRadius = 50f.dpToPx()
                isCheckedIconVisible = false
            }
            chipGroup.addView(chip)
        }

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
                val categoryId = selectedChip?.tag as? Int
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
            val action = ExploreEventFragmentDirections.actionExploreEventToDetailEvent(event)
            findNavController().navigate(action)
        }
        binding.rvListEvents.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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