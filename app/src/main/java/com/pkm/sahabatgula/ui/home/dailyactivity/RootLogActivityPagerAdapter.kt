package com.pkm.sahabatgula.ui.home.dailyactivity

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pkm.sahabatgula.ui.home.dailyactivity.activity.ActivityFragment
import com.pkm.sahabatgula.ui.home.dailyactivity.logactivity.LogActivityFragment

class RootLogActivityPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return if(position == 0) ActivityFragment() else LogActivityFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }
}