package com.pkm.sahabatgula.onboardingscreen

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


data class OnboardingItem(
    val imageRes: Int,
    val title: String,
    val desc: String
)

class OnboardingAdapter(
    fragment: Fragment,
    private val items: List<OnboardingItem> // (imageResId, title, desc)
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val (imageResId, title, desc) = items[position]
        return OnboardingItemFragment.newInstance(imageResId, title, desc)
    }
}