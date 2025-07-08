package com.example.ideapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 3 // Home, Categories, My Apps

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> CategoriesFragment.newInstance()
            2 -> MyAppsFragment.newInstance()
            else -> HomeFragment.newInstance()
        }
    }
}
