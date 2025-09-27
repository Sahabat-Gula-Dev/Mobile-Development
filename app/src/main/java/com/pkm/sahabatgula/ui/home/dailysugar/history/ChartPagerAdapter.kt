package com.pkm.sahabatgula.ui.home.dailysugar.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailysugar.history.weekly.WeeklySugarFragment
import com.pkm.sahabatgula.ui.home.dailysugar.history.monthly.MonthlySugarFragment

class ChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklySugarFragment() else MonthlySugarFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}