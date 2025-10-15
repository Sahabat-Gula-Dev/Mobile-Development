package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.databinding.FragmentLogManualFoodBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.LogFoodFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogManualFoodFragment : Fragment() {

    private var _binding: FragmentLogManualFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogManualFoodViewModel by viewModels()
    private lateinit var pagingAdapter: FoodPagingAdapter

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
        setupSearchAndNavigate()
    }

    private fun setupRecyclerView() {
        pagingAdapter = FoodPagingAdapter { foodItemManual ->
            val action = LogFoodFragmentDirections.actionAddLogFoodToDetailFoodFragment(foodItemManual = foodItemManual, foodItem = null)
            findNavController().navigate(action)
        }
        binding.rvFood.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.foodPagingData.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.chipGroupFoodCategories.removeAllViews()
                    addCategoryChips(resource.data)
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> { }
            }
        }
    }

    private fun addCategoryChips(categories: List<FoodCategories>?) {
        val chipGroup = binding.chipGroupFoodCategories
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
                viewModel.setCategory(null)
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
                viewModel.setCategory(categoryId)
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

                parentFragmentManager.setFragmentResult("manualSearchKey", bundle)
                return@setOnEditorActionListener true
            }
            false
        }
    }
}
