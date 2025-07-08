package com.example.ideapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.R
import com.google.android.material.appbar.MaterialToolbar

class FinanceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_finance)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Finance"
        toolbar.setNavigationOnClickListener { finish() }
    }
}
