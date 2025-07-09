package com.example.ideapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// --- AdminActivity ---
// Ez az Activity egy egyszerű admin felületet mutat, ahol kijelentkezhetsz.
// A kijelentkezés után visszairányít az AdminLoginActivity-re.
class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Kijelentkezés gomb beállítása
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
    }

    // --- Kijelentkezés függvény ---
    private fun logout() {
        // Firebase Authentication kijelentkezés
        FirebaseAuth.getInstance().signOut()
        // Visszairányítás a főképernyőre (MainActivity)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Jelenlegi Activity bezárása
    }
}
