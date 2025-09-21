package com.pkm.sahabatgula.ui.auth.register.inputdatauser.highbloodglucose

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentInputDataUserHighBloodGlucoseBinding
import com.pkm.sahabatgula.ui.auth.register.inputdatauser.InputDataViewModel


class InputDataUserHighBloodGlucoseFragment : Fragment() {

    private var _binding: FragmentInputDataUserHighBloodGlucoseBinding? = null
    private val binding get() = _binding!!
    private val inputDataViewModel: InputDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputDataUserHighBloodGlucoseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListener()
        observeViewModel()
    }
    private fun observeViewModel() {
        TODO("Not yet implemented")
    }

    private fun setupClickListener() {
        TODO("Not yet implemented")
    }

}