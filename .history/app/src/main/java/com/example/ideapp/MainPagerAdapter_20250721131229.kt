package com.example.ideapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ideapp.AboutUsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4 // Home, Categories, My Apps, About Us

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> CategoriesFragment.newInstance()
            2 -> MyAppsFragment.newInstance()
            3 -> AboutUsFragment()
            else -> HomeFragment.newInstance()
        }
    }
}
