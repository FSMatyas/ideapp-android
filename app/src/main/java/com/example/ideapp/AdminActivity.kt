package com.example.ideapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// --- AdminActivity ---
// Ez az Activity egy egyszerű admin felületet mutat, ahol kijelentkezhetsz.
// A kijelentkezés után visszairányít az AdminLoginActivity-re.
class AdminActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdminAdapter
    private val cards = mutableListOf<Card>()
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private var cardsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to new AdminNavActivity with bottom nav
        val intent = Intent(this, AdminNavActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun listenForCards() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        val db = FirebaseFirestore.getInstance()
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            progressBar.visibility = View.GONE
            tvEmpty.text = "Admin not logged in!"
            tvEmpty.visibility = View.VISIBLE
            return
        }
        cardsListener = db.collection("ideas")
            .whereIn("status", listOf("APPROVED", "PENDING"))
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                cards.clear()
                if (error != null) {
                    tvEmpty.text = "Hiba történt: ${error.localizedMessage}"
                    tvEmpty.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (!snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val card = doc.toObject(Card::class.java)
                            if (card != null) {
                                card.id = doc.id // set Firestore doc ID
                                cards.add(card)
                            }
                        }
                        // Sort cards by createdAt descending (newest first)
                        cards.sortByDescending { it.createdAt?.toDate() }
                        adapter.notifyDataSetChanged()
                        tvEmpty.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        adapter.notifyDataSetChanged()
                        tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = View.VISIBLE
                }
            }
    }

    private fun onApproveClicked(card: Card) {
        updateCardStatus(card, "APPROVED")
    }

    private fun onRejectClicked(card: Card) {
        updateCardStatus(card, "REJECTED")
    }

    private fun updateCardStatus(card: Card, status: String) {
        FirebaseFirestore.getInstance()
            .collection("ideas")
            .document(card.id)
            .update("status", status)
    }

    override fun onDestroy() {
        super.onDestroy()
        cardsListener?.remove()
    }
}
