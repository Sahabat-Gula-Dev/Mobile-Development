package com.pkm.sahabatgula.ui.home.dailyfood.logfood.customfood

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
    ): View {
        _binding = FragmentLogManualCustomFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.loadProfile()


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    profile?.let {
                        maxSugar = it.max_sugar
                        maxCalories = it.max_calories
                    }
                }
            }
        }

        setupRecyclerView()
        setupSearch()
        setupChipsObserver()
        setupPagingDataObserver()
        setupLogStatusObserver()
        setupButtonStateObserver()

        binding.btnLogThisFood.setOnClickListener {
            val (names, totalCalories) = viewModel.getSelectedFoodNamesAndCalories()
            if (names.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal satu makanan.", Toast.LENGTH_SHORT).show()
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
                        val sugarPercent = if (maxSugar != null && maxSugar != 0.0) {
                            (food.sugar / maxSugar!!) * 100
                        } else 0.0
                        val caloriesPercent = if (maxCalories != null && maxCalories != 0) {
                            (food.calories.toDouble() / maxCalories!!) * 100
                        } else 0.0

                        val personalizedMessage = "Saat ini kamu telah mengkonsumsi gula sebanyak ${sugarPercent.toInt()}% " +
                                "dan kalori sebanyak ${caloriesPercent.toInt()}% dari batas konsumsi harianmu."

                        showLogFoodStateDialog(
                            DialogFoodUiState.Success(
                                title = "Yey! Sudah Tersimpan",
                                message = personalizedMessage,
                                imageRes = R.drawable.glubby_food,
                                calorieValue = food.calories,
                                carbo = food.carbo,
                                protein = food.protein,
                                fat = food.fat,
                                sugar = food.sugar,
                                sodium = food.sodium,
                                fiber = food.fiber,
                                kalium = food.kalium
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


        (binding.rvCustomFood.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)
            ?.supportsChangeAnimations = false

        pagingAdapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is androidx.paging.LoadState.Loading
            val isError = loadStates.refresh is androidx.paging.LoadState.Error
            val isEmpty = pagingAdapter.itemCount == 0 && loadStates.refresh is androidx.paging.LoadState.NotLoading

            when {
                isLoading -> {
                    binding.layoutEmpty.root.visibility = View.GONE
                    binding.rvCustomFood.visibility = View.GONE
                }
                isError -> {
                    binding.layoutEmpty.root.visibility = View.VISIBLE
                    binding.rvCustomFood.visibility = View.GONE
                    binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                    binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                    binding.layoutEmpty.tvMessage.text =
                        "Pencarian makanan tidak dapat dilakukan, periksa koneksi internet kamu atau muat ulang halaman"
                }
                isEmpty -> {
                    binding.layoutEmpty.root.visibility = View.VISIBLE
                    binding.rvCustomFood.visibility = View.GONE
                    binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                    binding.layoutEmpty.tvTitle.text = "Tidak Ada Hasil"
                    binding.layoutEmpty.tvMessage.text =
                        "Kami tidak menemukan apapun untuk pencarian ini. Coba gunakan kata kunci lain."
                }
                else -> {
                    binding.layoutEmpty.root.visibility = View.GONE
                    binding.rvCustomFood.visibility = View.VISIBLE
                }
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
                is Resource.Error -> Toast.makeText(context, "Terjadi kesalahan saat memuat kategori", Toast.LENGTH_SHORT).show()
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
            val radius = 50f.dpToPx()
                shapeAppearanceModel = shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(radius)
                    .build()
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
                val radius = 50f.dpToPx()
                shapeAppearanceModel = shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(radius)
                    .build()
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
                is Resource.Success, is Resource.Error -> {
                    binding.btnLogThisFood.isEnabled = true
                    binding.btnLogThisFood.text = "Catat Aktivitas"
                }
            }
        }
    }

    private fun setupButtonStateObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedFoodIdsState.collect { selectedIds ->
                    binding.btnLogThisFood.isEnabled = selectedIds.isNotEmpty()
                }
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

        val foodsName = foodNames.joinToString(", ")
        val calorieText = "$totalCalories kkal"

        val fullText =
            "Kamu akan mencatat $foodsName dengan total kalori sebanyak $calorieText. Pastikan ini tidak melebihi batas konsumsimu ya!"

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
