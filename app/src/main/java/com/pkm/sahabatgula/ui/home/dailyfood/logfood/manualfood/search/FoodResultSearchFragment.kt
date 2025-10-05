package com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pkm.sahabatgula.databinding.FragmentResultSearchBinding

class FoodResultSearchFragment : Fragment() {
     private var _binding: FragmentResultSearchBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
}