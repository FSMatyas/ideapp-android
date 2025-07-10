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
        try {
            setContentView(R.layout.activity_admin)
        } catch (e: Exception) {
            return
        }
        try {
            recyclerView = findViewById(R.id.recyclerViewCards)
            progressBar = findViewById(R.id.progressBar)
            tvEmpty = findViewById(R.id.tvEmpty)
            val btnLogout = findViewById<Button>(R.id.btnLogout)
            btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            return
        }
        try {
            adapter = CardAdminAdapter(cards, ::onApproveClicked, ::onRejectClicked) { card ->
                // Remove card from admin list
                val removed = cards.remove(card)
                adapter.notifyDataSetChanged()
                // Convert Card to Idea and add to InProgressCardStore
                val ideaStatus = when (card.status.uppercase()) {
                    "APPROVED" -> com.example.ideapp.data.IdeaStatus.APPROVED
                    "IN_DEVELOPMENT" -> com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT
                    "TESTING" -> com.example.ideapp.data.IdeaStatus.TESTING
                    "COMPLETED" -> com.example.ideapp.data.IdeaStatus.COMPLETED
                    "REJECTED" -> com.example.ideapp.data.IdeaStatus.REJECTED
                    else -> com.example.ideapp.data.IdeaStatus.PENDING
                }
                val idea = com.example.ideapp.data.Idea(
                    id = card.id,
                    title = card.title,
                    description = card.description,
                    submitterEmail = card.submitterEmail,
                    submitterName = card.submitterName,
                    creatorId = card.creatorId,
                    messages = card.messages.map {
                        com.example.ideapp.data.Message(
                            sender = it.sender,
                            text = it.text,
                            timestamp = it.timestamp
                        )
                    },
                    createdAt = card.createdAt,
                    status = ideaStatus
                )
                com.example.ideapp.InProgressCardStore.inProgressCards.add(idea)
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            return
        }

        // Összes kártya lekérdezése és megjelenítése
        listenForCards()
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
        updateCardStatus(card, "rejected")
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
