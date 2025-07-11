package com.example.ideapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ideapp.databinding.ActivityAdminLoginBinding
import com.google.firebase.auth.FirebaseAuth

// --- ADMIN LOGIN ACTIVITY ---
// Ez az Activity felelős az admin bejelentkezésért Firebase Authentication segítségével.
// A bejelentkezés email + jelszóval történik.
class AdminLoginActivity : AppCompatActivity() {
    // View binding az activity_admin_login.xml-hez
    private lateinit var binding: ActivityAdminLoginBinding
    // Firebase Auth példány
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth inicializálása
        auth = FirebaseAuth.getInstance()

        // --- Bejelentkezés gomb eseménykezelő ---
        binding.btnAdminLogin.setOnClickListener {
            val email = binding.etAdminEmail.text.toString().trim()
            val password = binding.etAdminPassword.text.toString()

            // Ellenőrzés: üres mezők
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Kérlek, töltsd ki az emailt és a jelszót!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Firebase Authentication bejelentkezés ---
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sikeres bejelentkezés", Toast.LENGTH_SHORT).show()
                        // Go to AdminNavActivity with bottom nav and clear back stack
                        val intent = android.content.Intent(this, AdminNavActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Ismeretlen hiba"
                        Toast.makeText(this, "Hiba: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
