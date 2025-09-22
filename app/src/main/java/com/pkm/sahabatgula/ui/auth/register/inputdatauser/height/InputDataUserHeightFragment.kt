package com.pkm.sahabatgula.ui.auth.register.inputdatauser.height

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputUserHeightBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.let

class InputDataUserHeightFragment : Fragment() {

    private var _binding: FragmentInputUserHeightBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()
    private var layoutManager: LinearLayoutManager? = null
    private val minValue = 60
    private val maxValue = 300
    private val defaultValue = 140
    private var valueHeight = defaultValue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputUserHeightBinding.inflate(inflater,container, false)
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
                inputDataViewModel.profileData.collect { state ->
                    binding.btnContinueToHeavy.isEnabled = state.height != null
                    binding.btnContinueToHeavy.setOnClickListener {
                        findNavController().navigate(R.id.input_height_to_input_heavy)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val rvRulerHeight = binding.componentRulerHeight.rulerRv
        val heightAdapter = RulerAdapterHeight(minValue, maxValue)
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        rvRulerHeight.apply {
            adapter = heightAdapter
            layoutManager = this@InputDataUserHeightFragment.layoutManager
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvRulerHeight)

        rvRulerHeight.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val centerView = snapHelper.findSnapView(layoutManager)
                centerView?.let {
                    val position: Int? = layoutManager?.getPosition(it)
                    val valueHeight = minValue + position!!
                    binding.componentRulerHeight.tvHeightValue.text = valueHeight.toString()
                    inputDataViewModel.selectHeight(valueHeight)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    centerView?.let {
                        val position: Int? = layoutManager?.getPosition(it)
                        val valueHeight = minValue + position!!
                        binding.componentRulerHeight.tvHeightValue.text = valueHeight.toString()
                        inputDataViewModel.selectHeight(valueHeight)
                    }
                }
            }
        })

        rvRulerHeight.post {
            rvRulerHeight.scrollToPosition(defaultValue - minValue)
            binding.componentRulerHeight.tvHeightValue.text = valueHeight.toString()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { profileData ->
                    binding.componentRulerHeight.tvHeightValue.text = profileData.height.toString()
                }
            }
        }
    }

}