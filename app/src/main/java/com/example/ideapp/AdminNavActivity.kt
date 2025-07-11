package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminNavActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_nav)

        val bottomNav = findViewById<BottomNavigationView>(R.id.admin_bottom_nav)
        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.admin_nav_host_fragment, AdminPanelFragment())
                .commit()
        }
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_home -> {
                    switchFragment(AdminPanelFragment())
                    true
                }
                R.id.nav_admin_work -> {
                    switchFragment(AdminWorkFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_nav_host_fragment, fragment)
            .commit()
    }
}
