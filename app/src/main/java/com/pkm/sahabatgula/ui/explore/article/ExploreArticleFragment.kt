@file:Suppress("DEPRECATION")

package com.pkm.sahabatgula.ui.explore.article

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView // <<< TAMBAHKAN IMPORT INI
import com.google.android.material.chip.Chip
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.ArticleCategory
import com.pkm.sahabatgula.databinding.FragmentExploreArticleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExploreArticleFragment : Fragment() {

    private var _binding: FragmentExploreArticleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreArticleViewModel by viewModels()
    private lateinit var articleAdapter: ArticlePagingDataAdapter
    private var searchJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupKeyboardListener()
        setupRecyclerView()
        setupSearchAndNavigate()
        observeArticles()
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
            binding.rvListArticles.setPadding(
                binding.rvListArticles.paddingLeft,
                binding.rvListArticles.paddingTop,
                binding.rvListArticles.paddingRight,
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

        binding.rvListArticles.setOnTouchListener { _, _ ->
            if (binding.searchEditText.hasFocus()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
            }
            false
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                    is Resource.Loading -> { /* Tampilkan loading jika perlu */ }
                }
            }
        }
    }

    private fun setupChipGroup(categories: List<ArticleCategory>?) {
        val chipGroup = binding.chipGroupArticleCategories
        chipGroup.removeAllViews()

        val customTypefaceRegular =
            ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_regular)
        val customTypefaceBold =
            ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_bold)

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
                val categoryIdString = viewModel.selectedCategoryId.value.toString()

                val bundle = Bundle().apply {
                    putString("searchQuery", query)
                    putString("categoryId", categoryIdString)
                }

                findNavController().navigate(
                    R.id.action_exploreArticle_to_resultSearchArticle,
                    bundle
                )
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticlePagingDataAdapter { article ->
            val action = ExploreArticleFragmentDirections.actionExploreArticleToDetailArticle(article)
            findNavController().navigate(action)
        }
        binding.rvListArticles.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeArticles() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.articles.collectLatest { pagingData ->
                    Log.d("SearchDebug", "Mencoba submit PagingData baru ke adapter...")
                    articleAdapter.submitData(pagingData)
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