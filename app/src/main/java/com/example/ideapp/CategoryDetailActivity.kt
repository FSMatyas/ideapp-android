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
        val categoryKey = intent.getStringExtra("category_key")
        val categoryName = if (categoryKey != null) {
            val resId = resources.getIdentifier(categoryKey, "string", packageName)
            if (resId != 0) getString(resId) else ""
        } else {
            ""
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = categoryName
        toolbar.setNavigationOnClickListener { finish() }
        // Set the placeholder text in the center
        findViewById<android.widget.TextView>(R.id.tvCategoryPlaceholder).text = categoryName
        // Later, when you add content, hide tvCategoryPlaceholder
    }
}
