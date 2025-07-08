package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.R
import com.google.android.material.appbar.MaterialToolbar

class HealthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_health)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Health"
        toolbar.setNavigationOnClickListener { finish() }
    }
}
