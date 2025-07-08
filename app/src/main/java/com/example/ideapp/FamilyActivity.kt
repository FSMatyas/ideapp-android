package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.R
import com.google.android.material.appbar.MaterialToolbar

class FamilyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_family)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Family"
        toolbar.setNavigationOnClickListener { finish() }
    }
}
