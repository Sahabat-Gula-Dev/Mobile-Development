package com.pkm.sahabatgula.ui.settings.loghistory

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.settings.loghistory.activityhistory.ActivityHistoryFragment
import com.pkm.sahabatgula.ui.settings.loghistory.foodhistory.FoodHistoryFragment

class LogHistoryPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) FoodHistoryFragment() else ActivityHistoryFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}