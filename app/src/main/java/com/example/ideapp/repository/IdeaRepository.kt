package com.example.ideapp.repository

import com.google.firebase.firestore.FirebaseFirestore

object IdeaRepository {
    fun sendUserReply(ideaId: String, reply: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        FirebaseFirestore.getInstance().collection("ideas").document(ideaId)
            .update("userReply", reply)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
