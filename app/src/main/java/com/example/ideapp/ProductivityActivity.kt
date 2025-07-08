package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.R
import com.google.android.material.appbar.MaterialToolbar

class ProductivityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productivity)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_productivity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Productivity"
        toolbar.setNavigationOnClickListener { finish() }
    }
}
