package com.pkm.sahabatgula.ui.settings.helpcenter

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.data.remote.model.FaqCategories
import com.pkm.sahabatgula.databinding.FragmentHelpCenterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpCenterFragment : Fragment() {

    private var _binding: FragmentHelpCenterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HelpCenterViewModel by viewModels()
    private lateinit var faqAdapter: FaqPagingDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelpCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeFaqs()
        observeCategories()
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqPagingDataAdapter()
        binding.rvFaqs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = faqAdapter
        }
    }

    private fun observeFaqs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.faqs.collectLatest { pagingData ->
                faqAdapter.submitData(pagingData)
            }
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                if (categories.isNotEmpty()) {
                    setupChipGroup(categories)
                }
            }
        }
    }

    // Mengadaptasi fungsi setupChipGroup yang sudah kamu buat
    private fun setupChipGroup(categories: List<FaqCategories>?) {
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

        // Buat Chip "Semua"
        val allChip = Chip(context).apply {
            text = "Semua"
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
            chipBackgroundColor = backgroundColorStateList
            setTextColor(textColorStateList)
            setChipStrokeColorResource(R.color.md_theme_outline)
            chipStrokeWidth = 1f.dpToPx()
            val radius = 50f.dpToPx()
                shapeAppearanceModel = shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(radius)
                    .build()
            isCheckedIconVisible = false
            typeface = customTypefaceBold
            textSize = 12f
        }

        chipGroup.addView(allChip)
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = true

        categories?.forEach { category ->
            val chip = Chip(context).apply {
                text = category.name
                isCheckable = true
                id = View.generateViewId()
                tag = category.id
                chipBackgroundColor = backgroundColorStateList
                setTextColor(textColorStateList)
                setChipStrokeColorResource(R.color.md_theme_outline)
                chipStrokeWidth = 1f.dpToPx()
                val radius = 50f.dpToPx()
                shapeAppearanceModel = shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(radius)
                    .build()
                isCheckedIconVisible = false
                typeface = customTypefaceRegular
                textSize = 12f // Sesuaikan
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val selectedId = checkedIds.first()

            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.typeface = if (chip.id == selectedId) customTypefaceBold else customTypefaceRegular
            }

            val selectedChip = group.findViewById<Chip>(selectedId)
            val categoryId = selectedChip?.tag as? Int
            viewModel.selectCategory(categoryId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}