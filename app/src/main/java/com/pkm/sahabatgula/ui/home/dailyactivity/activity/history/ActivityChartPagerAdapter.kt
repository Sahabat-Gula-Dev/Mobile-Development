package com.pkm.sahabatgula.ui.home.dailyactivity.activity.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.monthly.MonthlyActivityFragment
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.weekly.WeeklyActivityFragment
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.monthly.MonthlyFoodFragment
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.weekly.WeeklyFoodFragment

class ActivityChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyActivityFragment() else MonthlyActivityFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}