package com.pkm.sahabatgula.ui.home.dailycarbo.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.monthly.MonthlyActivityFragment
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.history.weekly.WeeklyActivityFragment
import com.pkm.sahabatgula.ui.home.dailycarbo.history.monthly.MonthlyCarboFragment
import com.pkm.sahabatgula.ui.home.dailycarbo.history.weekly.WeeklyCarboFragment
import com.pkm.sahabatgula.ui.home.dailysugar.history.monthly.MonthlySugarFragment
import com.pkm.sahabatgula.ui.home.dailysugar.history.weekly.WeeklySugarFragment

class CarboChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyCarboFragment() else MonthlyCarboFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}