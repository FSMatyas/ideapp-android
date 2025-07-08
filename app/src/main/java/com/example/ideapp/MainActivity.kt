package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)
        // Optionally, set the default page to Home
        viewPager.currentItem = 0

        // Optionally, sync with bottom navigation (if you want both)
        setupBottomNavigation(viewPager)
    }

    private fun setupBottomNavigation(viewPager: ViewPager2) {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_categories -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_my_apps -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
        // Sync ViewPager2 swipes with bottom navigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNavigation.selectedItemId = R.id.nav_home
                    1 -> bottomNavigation.selectedItemId = R.id.nav_categories
                    2 -> bottomNavigation.selectedItemId = R.id.nav_my_apps
                }
            }
        })
    }
}
