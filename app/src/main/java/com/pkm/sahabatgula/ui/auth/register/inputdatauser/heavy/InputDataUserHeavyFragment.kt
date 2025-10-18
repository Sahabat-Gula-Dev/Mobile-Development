package com.pkm.sahabatgula.ui.auth.register.inputdatauser.heavy

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputDataUserHeavyBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch

class InputDataUserHeavyFragment : Fragment() {

    private var _binding: FragmentInputDataUserHeavyBinding? = null
    private val binding: FragmentInputDataUserHeavyBinding get() = _binding!!
    private val viewModel: InputDataViewModel by activityViewModels()
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var weightAdapter: HeavyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputDataUserHeavyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupClickListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileData.collect { state ->
                    binding.btnContinueToWaistCircumference.isEnabled = state.age != null
                    binding.btnContinueToWaistCircumference.setOnClickListener {
                        findNavController().navigate(R.id.input_heavy_to_input_waist_circumferences)
                    }
                }
            }
        }
    }

    // Ganti seluruh fungsi observeViewModel dengan ini
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileData.collect { state ->
                    val weight = state.weight ?: 45 // Ambil nilai dari state, default 45

                    // 1. Update TextView yang di tengah
                    binding.tvSelectedWeight.text = weight.toString()
                    Log.d("ViewModelObserver", "Observed weight: $weight") // Tambahkan log untuk debug

                    // 2. Update posisi adapter agar visualnya (warna/ukuran) benar
                    val newPosition = weightAdapter.getPositionForWeight(weight)
                    if (weightAdapter.selectedPosition != newPosition) {
                        val oldPosition = weightAdapter.selectedPosition
                        weightAdapter.selectedPosition = newPosition

                        // Notify perubahan untuk posisi lama dan baru untuk re-render
                        if (oldPosition != -1) {
                            weightAdapter.notifyItemChanged(oldPosition)
                        }
                        if (newPosition != -1) {
                            weightAdapter.notifyItemChanged(newPosition)
                        }
                    }

                    // 3. Sinkronkan posisi scroll RecyclerView jika tidak sinkron
                    val currentCenterPosition = findCenterPosition()
                    if (currentCenterPosition != newPosition) {
                        binding.rvWeight.post {
                            layoutManager?.scrollToPositionWithOffset(newPosition, 0)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        weightAdapter = HeavyAdapter()
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvWeight.apply {
            adapter = weightAdapter
            this.layoutManager = this@InputDataUserHeavyFragment.layoutManager


            addItemDecoration(object: RecyclerView.ItemDecoration(){
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val itemSpacing = 16.dpToPx()

                    when (position) {
                        0 -> {
                            outRect.top = 148.dpToPx()
                            outRect.bottom = itemSpacing-23
                        }

                        weightAdapter.itemCount - 1 -> {
                            // Item terakhir
                            outRect.bottom = 147.dpToPx()
                            outRect.top = itemSpacing
                        }

                        else -> {
                            // Item lainnya
                            outRect.top = itemSpacing
                            outRect.bottom = itemSpacing
                        }
                    }

                }
            })

            // Ganti bagian addOnScrollListener di dalam setupRecyclerView
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // Hanya update ViewModel saat scroll benar-benar berhenti
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val centerPosition = findCenterPosition()
                        if (centerPosition != RecyclerView.NO_POSITION) {
                            val weight = weightAdapter.getWeightAtPosition(centerPosition)
                            // Kirim event ke ViewModel, biarkan ViewModel yang mengelola state
                            if (weight != viewModel.profileData.value.weight) {
                                viewModel.selectWeight(weight)
                            }
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Tetap jalankan efek visual saat scrolling
                    applyScaleEffectToChildren()
                    // Anda bisa memperbarui teks di sini untuk feedback instan,
                    // tapi pembaruan final dan authoritatif datang dari observer
                    updateSelectedWeightDisplay()
                }
            })
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvWeight)

        // Setup initial position
        setupInitialPosition(snapHelper)

    }

    private fun setupInitialPosition(snapHelper: LinearSnapHelper) {
        binding.rvWeight.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                binding.rvWeight.removeOnLayoutChangeListener(this)
                val initialWeight = viewModel.profileData.value.weight ?: 45
                val selectedPosition = weightAdapter.getPositionForWeight(initialWeight)

                binding.rvWeight.scrollToPosition(selectedPosition)
                binding.rvWeight.post {
                    val view = layoutManager?.findViewByPosition(selectedPosition)
                    if (view != null) {
                        val distance = snapHelper.calculateDistanceToFinalSnap(layoutManager!!, view)
                        if (distance != null) {
                            binding.rvWeight.smoothScrollBy(distance[0], distance[1])
                        }
                    }
                    binding.tvSelectedWeight.text = initialWeight.toString()
                }
            }

        })
    }

    private fun updateSelectedWeightDisplay() {
        val centerPosition = findCenterPosition()
        if(centerPosition != RecyclerView.NO_POSITION) {
            val weight = weightAdapter.getWeightAtPosition(centerPosition)
            binding.tvSelectedWeight.text = weight.toString()
        }
    }

    private fun findCenterPosition(): Int {
        val layoutManager = this.layoutManager ?: return RecyclerView.NO_POSITION
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        var closestChild: View? = null
        var closestDistance = Int.MAX_VALUE
        val centerY = binding.rvWeight.height / 2

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val child = layoutManager.findViewByPosition(i)
            child?.let {
                val childCenterY = (it.top + it.bottom) / 2
                val distance = kotlin.math.abs(centerY - childCenterY)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestChild = it
                }
            }
        }

        return closestChild?.let { binding.rvWeight.getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
    }

    private fun applyScaleEffectToChildren() {
        val recyclerView = binding.rvWeight
        val centerY = recyclerView.height / 2f

        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)

            val childCenterY = (child.top + child.bottom) / 2f
            val distanceFromCenter = kotlin.math.abs(centerY - childCenterY)

            val maxDistance = centerY
            val scale = 0.9f + (1.7f - 0.5f) * (1 - kotlin.math.min(distanceFromCenter / maxDistance, 1f))

            child.scaleX = scale
            child.scaleY = scale

            val textView = child.findViewById<TextView>(R.id.tvWeight)
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
            if (distanceFromCenter < child.height / 2f) {
                child.setPadding(0, padding, 0, padding)
            } else {
                child.setPadding(0, 10, 0, 10)
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