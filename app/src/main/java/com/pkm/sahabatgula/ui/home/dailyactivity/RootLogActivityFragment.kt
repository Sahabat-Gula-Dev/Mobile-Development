package com.pkm.sahabatgula.ui.home.dailyactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.databinding.FragmentRootLogActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RootLogActivityFragment: Fragment() {

    private var _binding : FragmentRootLogActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRootLogActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabLayoutLogActivity = binding.tabLayoutLogActivity
        val viewPager = binding.viewPager
        viewPager.adapter = RootLogActivityPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(tabLayoutLogActivity, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Aktivitas"
                1 -> tab.text = "Catat Aktivitas"
            }
        }.attach()
    }

}
