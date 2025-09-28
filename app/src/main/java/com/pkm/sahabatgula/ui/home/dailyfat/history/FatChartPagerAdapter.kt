package com.pkm.sahabatgula.ui.home.dailyfat.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyfat.history.monthly.MonthlyFatFragment
import com.pkm.sahabatgula.ui.home.dailyfat.history.weekly.WeeklyFatFragment
import com.pkm.sahabatgula.ui.home.dailyprotein.history.monthly.MonthlyProteinFragment
import com.pkm.sahabatgula.ui.home.dailyprotein.history.weekly.WeeklyProteinFragment

class FatChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyFatFragment() else MonthlyFatFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}