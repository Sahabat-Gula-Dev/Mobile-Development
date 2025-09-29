package com.pkm.sahabatgula.ui.home.dailyfood.logfood

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.manualfood.LogManualFoodFragment
import com.pkm.sahabatgula.ui.home.dailyfood.logfood.scanning.FoodScanFragment

class LogFoodPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) FoodScanFragment() else LogManualFoodFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}