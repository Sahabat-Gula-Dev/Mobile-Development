package com.pkm.sahabatgula.ui.auth.register.inputdatauser.age

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputDataUserAgeBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class InputDataUserAgeFragment : Fragment() {

    private var _binding: FragmentInputDataUserAgeBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()
    private lateinit var ageAdapter: AgePickerAdapter
    private var layoutManager: LinearLayoutManager? = null
    // Flag untuk mencegah loop update
    private var isUpdatingFromScroll = false
    private var isUpdatingFromViewModel = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputDataUserAgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        ageAdapter = AgePickerAdapter()
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.rvAge.apply {
            adapter = ageAdapter
            layoutManager = this@InputDataUserAgeFragment.layoutManager

            addItemDecoration(object: RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val itemSpacing = 16.dpToPx()

                    when(position) {
                        0 -> {
                            outRect.left = 160.dpToPx()
                            outRect.right = itemSpacing
                        }
                        1 -> {
                            outRect.left = itemSpacing-36
                        }
                        ageAdapter.itemCount - 1 -> {
                            outRect.right = 158.dpToPx()
                            outRect.left = itemSpacing-23
                        }
                        else -> {
                            outRect.left = itemSpacing
                            outRect.right = itemSpacing
                        }
                    }
                }
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !isUpdatingFromViewModel) {
                        updateSelectedAgeFromScroll()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Hanya update visual effects, tidak update ViewModel
                    if (!isUpdatingFromViewModel) {
                        applyScaleEffectToChildren()
                        updateSelectedAgeDisplay() // Update display tanpa update ViewModel
                    }
                }
            })
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvAge)

        // Setup initial position
        setupInitialPosition(snapHelper)
    }

    private fun setupInitialPosition(snapHelper: LinearSnapHelper) {
        binding.rvAge.addOnLayoutChangeListener(object :View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                binding.rvAge.removeOnLayoutChangeListener(this)
                val initialAge = inputDataViewModel.profileData.value.age?:20
                val selectedPosition = ageAdapter.getPositionForAge(initialAge)

                binding.rvAge.scrollToPosition(selectedPosition)
                binding.rvAge.post {
                    val view = layoutManager?.findViewByPosition(selectedPosition)
                    if (view != null) {
                        val distance = snapHelper.calculateDistanceToFinalSnap(layoutManager!!, view)
                        if (distance != null) {
                            binding.rvAge.smoothScrollBy(distance[0], distance[1])
                        }
                    }
                    // Update display setelah scroll selesai
                    binding.tvSelectedAge.text = initialAge.toString()
                }
            }
        })
    }


    private fun updateSelectedAgeDisplay() {
        val centerPosition = findCenterPosition()
        if (centerPosition != RecyclerView.NO_POSITION) {
            val age = ageAdapter.getAgeAtPosition(centerPosition)
            binding.tvSelectedAge.text = age.toString()
        }
    }


    private fun updateSelectedAgeFromScroll() {
        if (isUpdatingFromViewModel) return

        isUpdatingFromScroll = true
        val centerPosition = findCenterPosition()

        if (centerPosition != RecyclerView.NO_POSITION) {
            val age = ageAdapter.getAgeAtPosition(centerPosition)
            val currentAge = inputDataViewModel.profileData.value.age

            if (age != currentAge) {
                inputDataViewModel.selectAge(age)
                Log.d("AgePicker", "Usia dipilih lewat scroll: $age tahun")
            }
        }
        isUpdatingFromScroll = false
    }


    private fun findCenterPosition(): Int {
        val layoutManager = this.layoutManager ?: return RecyclerView.NO_POSITION
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        var closestChild: View? = null
        var closestDistance = Int.MAX_VALUE
        val centerX = binding.rvAge.width / 2

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val child = layoutManager.findViewByPosition(i)
            child?.let {
                val childCenterX = (it.left + it.right) / 2
                val distance = abs(centerX - childCenterX)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestChild = it
                }
            }
        }

        return closestChild?.let { binding.rvAge.getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
    }

    private fun applyScaleEffectToChildren() {
        val recyclerView = binding.rvAge
        val centerX = recyclerView.width / 2f

        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val childCenterX = (child.left + child.right) / 2f
            val distanceFromCenter = abs(centerX - childCenterX)
            val maxDistance = centerX
            val scale = 0.9f + (1.7f - 0.5f) * (1 - kotlin.math.min(distanceFromCenter / maxDistance, 1f))
            child.scaleX = scale
            child.scaleY = scale

            val textView = child.findViewById<TextView>(R.id.tvAge)
            val minTextSize = 18f
            val maxTextSize = 32f
            val textSize = minTextSize + (maxTextSize - minTextSize) * (1 - kotlin.math.min(distanceFromCenter / maxDistance, 1f))
            textView.textSize = textSize

            val textColor = when {
                distanceFromCenter < child.width * 3.5f -> ContextCompat.getColor(requireContext(), R.color.number_picker_v1)
                else -> ContextCompat.getColor(requireContext(), R.color.number_picker_v2)
            }
            textView.setTextColor(textColor)

            val padding = 36.dpToPx()
            if (distanceFromCenter < child.width / 2f) {
                child.setPadding(padding, 0, padding, 0)
            } else {
                child.setPadding(10, 0, 10, 0)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { state ->
                    if (isUpdatingFromScroll) return@collect // Skip update jika sedang scroll

                    isUpdatingFromViewModel = true
                    val age = state.age ?: 20

                    // Update display
                    binding.tvSelectedAge.text = age.toString()

                    // Update adapter selection
                    val currentPosition = ageAdapter.getPositionForAge(age)
                    if (ageAdapter.selectedPosition != currentPosition) {
                        val previousPosition = ageAdapter.selectedPosition
                        ageAdapter.selectedPosition = currentPosition

                        // Update adapter items
                        if (previousPosition != -1) {
                            ageAdapter.notifyItemChanged(previousPosition)
                        }
                        ageAdapter.notifyItemChanged(currentPosition)

                        // Scroll ke posisi baru (hanya jika perlu)
                        val layoutManager = this@InputDataUserAgeFragment.layoutManager
                        val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: -1
                        val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: -1

                        if (currentPosition < firstVisible || currentPosition > lastVisible) {
                            binding.rvAge.post {
                                binding.rvAge.smoothScrollToPosition(currentPosition)
                            }
                        }
                    }

                    isUpdatingFromViewModel = false
                }
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}

