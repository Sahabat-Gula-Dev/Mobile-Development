package com.pkm.sahabatgula.ui.home.dailywater.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailywater.history.monthly.MonthlyWaterFragment
import com.pkm.sahabatgula.ui.home.dailywater.history.weekly.WeeklyWaterFragment

class WaterChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyWaterFragment() else MonthlyWaterFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}