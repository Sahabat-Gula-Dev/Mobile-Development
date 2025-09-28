package com.pkm.sahabatgula.ui.home.dailyfood.charthistory

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.monthly.MonthlyFoodFragment
import com.pkm.sahabatgula.ui.home.dailyfood.charthistory.weekly.WeeklyFoodFragment

class FoodChartPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) WeeklyFoodFragment() else MonthlyFoodFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}