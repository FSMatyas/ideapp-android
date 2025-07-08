package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.R
import com.google.android.material.appbar.MaterialToolbar

class CategoryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_detail)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_category_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category Detail"
        toolbar.setNavigationOnClickListener { finish() }
    }
}
