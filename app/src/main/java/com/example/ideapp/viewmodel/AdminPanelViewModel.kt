package com.example.ideapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.example.ideapp.data.Idea

class AdminPanelViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _ideas = MutableLiveData<List<Idea>>()
    val ideas: LiveData<List<Idea>> = _ideas
    private var listener: ListenerRegistration? = null
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent: LiveData<Boolean> = _messageSent

    fun fetchIdeas() {
        listener?.remove()
        listener = db.collection("ideas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }
                val ideaList = snapshot?.documents?.mapNotNull { it.toObject<Idea>()?.copy(id = it.id) } ?: emptyList()
                _ideas.value = ideaList
            }
    }

    fun updateIdeaStatus(ideaId: String, status: String) {
        val statusEnum = when (status.lowercase()) {
            "pending" -> com.example.ideapp.data.IdeaStatus.PENDING
            "approved" -> com.example.ideapp.data.IdeaStatus.APPROVED
            "rejected" -> com.example.ideapp.data.IdeaStatus.REJECTED
            else -> com.example.ideapp.data.IdeaStatus.PENDING
        }
        db.collection("ideas").document(ideaId)
            .update("status", statusEnum)
            .addOnFailureListener { _error.value = it.localizedMessage }
    }

    fun sendAdminMessage(ideaId: String, message: String) {
        db.collection("ideas").document(ideaId)
            .update("adminNotes", message)
            .addOnSuccessListener { _messageSent.value = true }
            .addOnFailureListener { _error.value = it.localizedMessage }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
