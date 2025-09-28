package com.pkm.sahabatgula.ui.home.dailyprotein.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailycarbo.history.monthly.MonthlyCarboFragment
import com.pkm.sahabatgula.ui.home.dailycarbo.history.weekly.WeeklyCarboFragment
import com.pkm.sahabatgula.ui.home.dailyprotein.history.monthly.MonthlyProteinFragment
import com.pkm.sahabatgula.ui.home.dailyprotein.history.weekly.WeeklyProteinFragment

class ProteinChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyProteinFragment() else MonthlyProteinFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}