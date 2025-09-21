package com.pkm.sahabatgula.ui.auth.register.inputdatauser.waistcircumference

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
import com.pkm.sahabatgula.databinding.FragmentInputUserWaistCircumferenceBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.let

class InputDataUserWaistCircFragment : Fragment() {

    private var _binding: FragmentInputUserWaistCircumferenceBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()
    private var layoutManager: LinearLayoutManager? = null
    private val minValue = 20
    private val maxValue = 200
    private val defaultValue = 70
    private var valueWaistCirc = defaultValue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInputUserWaistCircumferenceBinding.inflate(inflater,container, false)
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
                    binding.btnContinueToBloodPressure.isEnabled = state.waistCircumference != null
                    binding.btnContinueToBloodPressure.setOnClickListener {
                        findNavController().navigate(R.id.input_waist_circumferences_to_input_blood_pressure)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        val rvRulerWaistCirc = binding.rulerComponentWaist.rulerRv
        val waistCircAdapter = RulerAdapterWaistCircumference(minValue, maxValue)
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        rvRulerWaistCirc.apply {
            adapter = waistCircAdapter
            layoutManager = this@InputDataUserWaistCircFragment.layoutManager
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvRulerWaistCirc)

        rvRulerWaistCirc.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val centerView = snapHelper.findSnapView(layoutManager)
                centerView?.let {
                    val position: Int? = layoutManager?.getPosition(it)
                    val valueWaistCirc = minValue + position!!
                    binding.rulerComponentWaist.tvHeightValue.text = valueWaistCirc.toString()
                    inputDataViewModel.selectWaistCirc(valueWaistCirc)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    centerView?.let {
                        val position: Int? = layoutManager?.getPosition(it)
                        val valueWaistCirc = minValue + position!!
                        binding.rulerComponentWaist.tvHeightValue.text = valueWaistCirc.toString()
                        inputDataViewModel.selectWaistCirc(valueWaistCirc)
                    }
                }
            }
        })

        rvRulerWaistCirc.post {
            rvRulerWaistCirc.scrollToPosition(defaultValue - minValue)
            binding.rulerComponentWaist.tvHeightValue.text = valueWaistCirc.toString()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                inputDataViewModel.profileData.collect { profileData ->
                    binding.rulerComponentWaist.tvHeightValue.text = profileData.waistCircumference.toString()
                }
            }
        }
    }

}