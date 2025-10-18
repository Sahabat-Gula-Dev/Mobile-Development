package com.pkm.sahabatgula.ui.home.dailystep

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pkm.sahabatgula.databinding.FragmentStepBinding

class StepFragment : Fragment() {

    private  var _binding: FragmentStepBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentStepBinding.inflate(inflater, container, false)
        return binding.root
    }


}