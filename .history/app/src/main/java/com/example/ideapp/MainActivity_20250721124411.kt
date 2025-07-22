package com.example.ideapp

import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private var batteryReceiver: BatteryLevelReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)
        // Optionally, set the default page to Home
        viewPager.currentItem = 0

        // Optionally, sync with bottom navigation (if you want both)
        setupBottomNavigation(viewPager)

        // Schedule a periodic background task using WorkManager (safe for Doze)
        val workRequest = PeriodicWorkRequestBuilder<BackgroundWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "background_task",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onStart() {
        super.onStart()
        // Register battery receiver
        batteryReceiver = BatteryLevelReceiver {
            runOnUiThread {
                Toast.makeText(this, "Battery is low! Some features may be limited.", Toast.LENGTH_LONG).show()
            }
        }
        registerReceiver(batteryReceiver, BatteryLevelReceiver.getIntentFilter())

        // --- Handle notification tap to open card ---
        intent?.getStringExtra("open_idea_id")?.let { ideaId ->
            // Find the HomeFragment and call a method to open the card
            val homeFragment = supportFragmentManager.fragments
                .flatMap { it.childFragmentManager.fragments }
                .find { it is com.example.ideapp.HomeFragment } as? com.example.ideapp.HomeFragment
            homeFragment?.openIdeaById(ideaId)
            // Remove the extra so it doesn't trigger again
            intent.removeExtra("open_idea_id")
        }
    }

    override fun onStop() {
        super.onStop()
        // Unregister battery receiver
        batteryReceiver?.let { unregisterReceiver(it) }
        batteryReceiver = null
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

// BackgroundWorker for safe background tasks (uses WorkManager, Doze/standby safe)
class BackgroundWorker(appContext: android.content.Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Do background work here (sync, notifications, etc.)
        // This will be deferred if device is in Doze or battery is low
        return Result.success()
    }
}
