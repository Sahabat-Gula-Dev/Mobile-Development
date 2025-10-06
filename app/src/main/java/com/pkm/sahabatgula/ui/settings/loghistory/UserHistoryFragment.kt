package com.pkm.sahabatgula.ui.settings.loghistory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.pkm.sahabatgula.R
import com.pkm.sahabatgula.databinding.FragmentScanBinding
import com.pkm.sahabatgula.databinding.FragmentUserHistoryBinding
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.LogFoodPagerAdapter

class UserHistoryFragment : Fragment() {
    private var _binding: FragmentUserHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayoutLogFood = binding.tabLayoutLogFood
        val viewPager = binding.viewPager
        viewPager.adapter = LogHistoryPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false


        TabLayoutMediator(tabLayoutLogFood, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Riwayat Konsumsi"
                1 -> tab.text = "Riwayat Aktivitas"
            }
        }.attach()
    }
}