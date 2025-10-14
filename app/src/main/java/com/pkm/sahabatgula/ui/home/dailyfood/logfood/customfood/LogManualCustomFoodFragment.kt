package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.core.Resource
import com.pkm.sahabatgula.data.remote.model.FoodCategories
import com.pkm.sahabatgula.databinding.FragmentLogManualCustomFoodBinding
import com.pkm.sahabatgula.ui.state.DialogFoodUiState
import com.pkm.sahabatgula.ui.state.LogFoodStateDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LogManualCustomFoodFragment : Fragment() {

    private var _binding: FragmentLogManualCustomFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogManualCustomFoodViewModel by viewModels()
    private lateinit var pagingAdapter: CustomFoodPagingAdapter
    private var searchJob: Job? = null

    private var maxSugar: Double? = null
    private var maxCalories: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogManualCustomFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.loadProfile()
        lifecycleScope.launchWhenStarted {
            viewModel.profile.collect { profile ->
                if (profile != null) {
                    maxSugar = profile.max_sugar
                    maxCalories = profile.max_calories
                }
            }
        }

        binding.btnLogThisFood.setOnClickListener {
            viewModel.logSelectedFoods()
        }

        binding.btnLogThisFood.setOnClickListener {
            val (names, totalCalories) = viewModel.getSelectedFoodNamesAndCalories()

            if (names.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal satu makanan.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            showMultipleFoodConfirmationDialog(names, totalCalories) {
                viewModel.logSelectedFoods()
            }
        }

        viewModel.logFoodStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        val food = viewModel.getSelectedFoodsSummaryFromDetail()
                        val sugarConsumed = food?. sugar ?: 0.0
                        val caloriesConsumed = food?.calories?.toInt() ?: 0

                        val sugarPercent = if (maxSugar != null && maxSugar != 0.0) {
                            (sugarConsumed / maxSugar!!) * 100
                        } else 0.0

                        val caloriesPercent = if (maxCalories != null && maxCalories != 0) {
                            (caloriesConsumed.toDouble() / maxCalories!!) * 100
                        } else 0.0

                        val sugarPercentRounded = String.format("%.0f", sugarPercent)
                        val caloriesPercentRounded = String.format("%.0f", caloriesPercent)

                        val personalizedMessage = "Saat ini kamu telah mengkonsumsi gula sebanyak $sugarPercentRounded% " + "dan kalori sebanyak $caloriesPercentRounded% dari batas konsumsi harianmu."

                        showLogFoodStateDialog(
                            DialogFoodUiState.Success(
                                title = "Yey! Sudah Tersimpan",
                                message = personalizedMessage,
                                imageRes = R.drawable.glubby_food,
                                calorieValue = food?.calories?.toInt(),
                                carbo = food?.carbo?.toInt(),
                                protein = food?.protein?.toInt(),
                                fat = food?.fat?.toInt(),
                                sugar = food?.sugar ?: 0.0,
                                sodium = food?.sodium ?: 0.0,
                                fiber = food?.fiber ?: 0.0,
                                kalium = food?.kalium ?: 0.0
                            )
                        )
                    }
                }

                is Resource.Error -> {
                    showLogFoodStateDialog(
                        DialogFoodUiState.Error(
                            title = "Oops, Ada Masalah",
                            message = status.message ?: "Terjadi kesalahan, coba lagi.",
                            imageRes = R.drawable.glubby_error
                        )
                    )
                }

                is Resource.Loading -> {}
            }
        }


        setupRecyclerView()
        setupSearch()
        setupChipsObserver()
        setupPagingDataObserver()
        setupLogStatusObserver()
        setupButtonStateObserver()
    }

    private fun setupRecyclerView() {
        pagingAdapter = CustomFoodPagingAdapter(
            onSelectClick = { foodItem -> viewModel.toggleFoodSelection(foodItem) },
            onExpandClick = { foodItem -> viewModel.onExpandClicked(foodItem) }
        )
        binding.rvCustomFood.apply {
            adapter = pagingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        pagingAdapter.addLoadStateListener { loadStates ->
            val isListEmpty = pagingAdapter.itemCount == 0 &&
                    loadStates.refresh is androidx.paging.LoadState.NotLoading

            if (isListEmpty) {
                // Tidak ada hasil pencarian
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                binding.layoutEmpty.tvTitle.text = "Tidak Ada Hasil"
                binding.layoutEmpty.tvMessage.text = "Kami tidak menemukan apapun untuk pencarian ini. Coba gunakan kata kunci lain."
                binding.rvCustomFood.visibility = View.GONE
            } else {
                binding.layoutEmpty.root.visibility = View.GONE
                binding.rvCustomFood.visibility = View.VISIBLE
            }

            // Kalau error saat load
            val errorState = loadStates.refresh as? androidx.paging.LoadState.Error
            if (errorState != null) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                binding.layoutEmpty.tvMessage.text = "Pencarian makanan tidak dapat dilakukan, periksa koneksi internet kamu atau muat ulang halaman"
            }
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
                    binding.chipGroupFoodCategories.removeAllViews()
                    addCategoryChips(resource.data)
                }

                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT)
                    .show()

                is Resource.Loading -> {}
            }
        }
    }

    private fun addCategoryChips(categories: List<FoodCategories>?) {
        val chipGroup = binding.chipGroupFoodCategories
        chipGroup.removeAllViews()

        val regular = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_regular)
        val bold = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_bold)

        val bgStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val bgColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
            ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary)
        )
        val bgColorStateList = ColorStateList(bgStates, bgColors)

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
            chipBackgroundColor = bgColorStateList
            setTextColor(textColorStateList)
            setChipStrokeColorResource(R.color.md_theme_outline)
            chipStrokeWidth = 1f.dpToPx()
            chipCornerRadius = 50f.dpToPx()
            isCheckedIconVisible = false
            typeface = bold
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
                chipBackgroundColor = bgColorStateList
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
                chip.typeface = if (chip.id == selectedId) bold else regular
            }
            selectedId?.let {
                val selectedChip = group.findViewById<Chip>(it)
                val categoryId = selectedChip?.tag as? Int
                viewModel.setCategory(categoryId)
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
                    viewModel.searchFood(query.ifEmpty { null })
                }
            }
        }
        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                viewModel.searchFood(query.ifEmpty { null })
                textView.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.searchBarFood.setEndIconOnClickListener {
            editText.text?.clear()
            editText.hideKeyboard()
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setupLogStatusObserver() {
        viewModel.logFoodStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.btnLogThisFood.isEnabled = false
                is Resource.Success -> {
                    binding.btnLogThisFood.isEnabled = true
                    binding.btnLogThisFood.text = "Catat Aktivitas"
                }
                is Resource.Error -> {
                    binding.btnLogThisFood.isEnabled = true
                    binding.btnLogThisFood.text = "Catat Aktivitas"
                }
            }
        }
    }

    private fun setupButtonStateObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedFoodIds.collect { selectedIds ->
                binding.btnLogThisFood.isEnabled = selectedIds.isNotEmpty()
            }
        }
    }

    private fun showMultipleFoodConfirmationDialog(
        foodNames: List<String>,
        totalCalories: Int,
        onConfirm: () -> Unit
    ) {
        val context = requireContext()
        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.glubby_calculate)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            val size = context.resources.getDimensionPixelSize(R.dimen.dialog_image_size)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
                bottomMargin = 16
                topMargin = 32
            }
        }

        val titleView = TextView(context).apply {
            text = "Catat Makanan Ini?"
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
            setPadding(16, 0, 16, 8)
        }

        // Format teks dengan angka kalori yang di-bold dan merah
        val foodsName = foodNames.joinToString(", ")
        val calorieText = "$totalCalories kkal"

        val fullText =
            "Kamu akan mencatat $foodsName dengan total kalori sebanyak $calorieText. Lanjutkan?"

        val spannable = android.text.SpannableString(fullText)

        val calorieStart = fullText.indexOf(calorieText)
        val calorieEnd = calorieStart + calorieText.length

        spannable.setSpan(
            android.text.style.ForegroundColorSpan(Color.RED),
            calorieStart,
            calorieEnd,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            calorieStart,
            calorieEnd,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val caloriesView = TextView(context).apply {
            text = spannable
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_regular)
            setPadding(16, 8, 16, 16)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimary))
            setPadding(24, 16, 24, 16)
            addView(imageView)
            addView(titleView)
            addView(caloriesView)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(container)
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .setPositiveButton("Catat") { d, _ ->
                onConfirm()
                d.dismiss()
            }
            .create()

        dialog.show()

        val onPrimary = ContextCompat.getColor(context, R.color.md_theme_onPrimary)
        val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        val customFont = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)

        (positiveButton.parent as? View)?.setBackgroundColor(onPrimary)
        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        negativeButton.setBackgroundColor(Color.TRANSPARENT)

        positiveButton.setTextColor(Color.BLACK)
        negativeButton.setTextColor(Color.BLACK)
        positiveButton.typeface = customFont
        negativeButton.typeface = customFont
    }

    private fun showLogFoodStateDialog(state: DialogFoodUiState) {
        val dialog = LogFoodStateDialogFragment.newInstance(state)
        dialog.show(parentFragmentManager, "LogFoodStateDialog")
        dialog.dismissListener = {
            findNavController().navigate(R.id.action_log_custom_food_fragment_to_home_graph)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}