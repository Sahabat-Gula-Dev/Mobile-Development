package com.pkm.sahabatgula.ui.home.dailyactivity.logactivity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import com.pkm.sahabatgula.data.remote.model.ActivityCategories
import com.pkm.sahabatgula.databinding.FragmentLogActivityBinding
import com.pkm.sahabatgula.ui.state.DialogFoodUiState
import com.pkm.sahabatgula.ui.state.LogFoodStateDialogFragment
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


        binding.btnLogThisActivity .setOnClickListener {
            val (names, totalCalories) = viewModel.getSelectedActivityNamesAndCalories()

            if (names.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal satu makanan.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            showMultipleActivityConfirmationDialog(names, totalCalories) {
                viewModel.logSelectedActivities()
            }
        }

        viewModel.logActivityStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        val summary = viewModel.getSelectedActivitiesSummary()
                        showLogActivityStateDialog(
                            DialogFoodUiState.Success(
                                title = "Yey! Sudah Tersimpan",
                                message = "Gluby sudah menyimpan aktivitasmu. Semakin aktif, semakin sehat",
                                imageRes = R.drawable.glubby_activity,
                                calorieValue = summary.totalCalories
                            )
                        )
                    }
                }

                is Resource.Error -> {
                    showLogActivityStateDialog(
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

        pagingAdapter.addLoadStateListener { loadStates ->
            val isListEmpty = pagingAdapter.itemCount == 0 &&
                    loadStates.refresh is androidx.paging.LoadState.NotLoading

            if (isListEmpty) {
                // Tidak ada hasil pencarian
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_not_found)
                binding.layoutEmpty.tvTitle.text = "Tidak Ada Hasil"
                binding.layoutEmpty.tvMessage.text = "Kami tidak menemukan apapun untuk pencarian ini. Coba gunakan kata kunci lain."
                binding.rvActivity.visibility = View.GONE
            } else {
                binding.layoutEmpty.root.visibility = View.GONE
                binding.rvActivity.visibility = View.VISIBLE
            }

            // Kalau error saat load
            val errorState = loadStates.refresh as? androidx.paging.LoadState.Error
            if (errorState != null) {
                binding.layoutEmpty.root.visibility = View.VISIBLE
                binding.layoutEmpty.imgGlubby.setImageResource(R.drawable.glubby_error)
                binding.layoutEmpty.tvTitle.text = "Oops.. Ada Error"
                binding.layoutEmpty.tvMessage.text = "Pencarian aktivitas tidak dapat dilakukan, periksa koneksi internet kamu atau muat ulang halaman"
            }
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
                    binding.chipGroupActivityCategories.removeAllViews()
                    addCategoryChips(resource.data)
                }
                is Resource.Error -> Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                is Resource.Loading -> {  }
            }
        }
    }
    
    private fun addCategoryChips(categories: List<ActivityCategories>?) {
        val chipGroup = binding.chipGroupActivityCategories
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

        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                viewModel.searchActivity(query.ifEmpty { null })

                textView.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.searchBarActivity.setEndIconOnClickListener {
            editText.text?.clear()
            editText.hideKeyboard()
        }
    }
    
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    
    private fun setupLogStatusObserver() {
        viewModel.logActivityStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                }
                is Resource.Success -> {
                    binding.btnLogThisActivity.isEnabled = true
                    Toast.makeText(context, "Aktivitas berhasil dicatat!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.btnLogThisActivity.isEnabled = true
                    Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showMultipleActivityConfirmationDialog(
        activityNames: List<String>,
        totalCalories: Int,
        onConfirm: () -> Unit
    ) {
        val context = requireContext()
        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.glubby_activity)
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
            text = "Catat Aktivitas Ini?"
            gravity = Gravity.CENTER
            textSize = 18f
            setTextColor(Color.BLACK)
            typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
            setPadding(16, 0, 16, 8)
        }

        val foodsName = activityNames.joinToString(", ")
        val calorieText = "$totalCalories kkal"

        val fullText =
            "Kamu akan mencatat $foodsName sehingga total kalori yang akan terbakar sebanyak $calorieText. Lanjutkan?"

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

    private fun showLogActivityStateDialog(state: DialogFoodUiState) {
        val dialog = LogFoodStateDialogFragment.newInstance(state)
        dialog.dismissListener = {
            findNavController()
                .getBackStackEntry(R.id.home_graph)
                .savedStateHandle["open_activity_tab_index"] = 0
            findNavController().navigate(R.id.action_log_activity_to_root_log_history)
        }

        dialog.show(parentFragmentManager, "LogFoodStateDialog")
    }

    private fun setupButtonStateObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedActivityIds.collect { selectedIds ->
                binding.btnLogThisActivity.isEnabled = selectedIds.isNotEmpty()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
