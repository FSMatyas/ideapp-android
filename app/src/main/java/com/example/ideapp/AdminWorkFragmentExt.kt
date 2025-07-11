package com.example.ideapp

import android.widget.Toast
import com.example.ideapp.data.Idea
import com.google.firebase.firestore.FirebaseFirestore

fun AdminWorkFragment.onCompleteClicked(idea: Idea) {
    val db = FirebaseFirestore.getInstance()
    db.collection("ideas").document(idea.id)
        .update("status", com.example.ideapp.data.IdeaStatus.COMPLETED)
        .addOnSuccessListener {
            Toast.makeText(requireContext(), "Átállítva készre!", Toast.LENGTH_SHORT).show()
            // Refresh the list
            this@onCompleteClicked.fetchInDevelopmentIdeas()
        }
        .addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Hiba: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
}
