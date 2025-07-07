package com.example.ideapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ideapp.data.Idea
import com.example.ideapp.data.IdeaStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val ideasCollection = firestore.collection("ideas")
    
    private val _ideas = MutableLiveData<List<Idea>>()
    val ideas: LiveData<List<Idea>> = _ideas
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadIdeas()
    }

    /**
     * Load all approved ideas from Firestore
     */
    private fun loadIdeas() {
        _isLoading.value = true
        
        // Temporary: Load ALL ideas to debug what's in the database
        ideasCollection
            .addSnapshotListener { snapshot, exception ->
                _isLoading.value = false
                
                if (exception != null) {
                    _error.value = "Failed to load ideas: ${exception.message}"
                    return@addSnapshotListener
                }
                
                val ideaList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Idea::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                _ideas.value = ideaList
            }
    }

    /**
     * Get ideas by category
     */
    fun getIdeasByCategory(category: String): LiveData<List<Idea>> {
        val categoryIdeas = MutableLiveData<List<Idea>>()
        
        ideasCollection
            .whereEqualTo("category", category)
            .whereEqualTo("isApproved", true)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _error.value = "Failed to load category ideas: ${exception.message}"
                    return@addSnapshotListener
                }
                
                val ideaList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Idea::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                categoryIdeas.value = ideaList
            }
        
        return categoryIdeas
    }

    /**
     * Submit a new idea to Firestore
     */
    suspend fun submitIdea(idea: Idea): Result<String> {
        return try {
            _isLoading.value = true
            
            // Check if this is the user's first idea (simple email check)
            val isFirstIdea = checkIfFirstIdeaByUser(idea.submitterEmail)
            
            val ideaToSubmit = idea.copy(
                isFirstIdeaByUser = isFirstIdea,
                isApproved = isFirstIdea, // Auto-approve first idea
                status = if (isFirstIdea) IdeaStatus.APPROVED else IdeaStatus.PENDING
            )
            
            val documentRef = ideasCollection.add(ideaToSubmit).await()
            _isLoading.value = false
            
            Result.success(documentRef.id)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to submit idea: ${e.message}"
            Result.failure(e)
        }
    }

    /**
     * Check if this is the user's first idea submission
     */
    private suspend fun checkIfFirstIdeaByUser(email: String): Boolean {
        return try {
            val existingIdeas = ideasCollection
                .whereEqualTo("submitterEmail", email)
                .limit(1)
                .get()
                .await()
            
            existingIdeas.isEmpty
        } catch (e: Exception) {
            false // Default to not first idea if check fails
        }
    }

    /**
     * Get ideas by submitter email (for My Apps section)
     */
    fun getIdeasBySubmitter(email: String): LiveData<List<Idea>> {
        val userIdeas = MutableLiveData<List<Idea>>()
        
        ideasCollection
            .whereEqualTo("submitterEmail", email)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _error.value = "Failed to load your ideas: ${exception.message}"
                    return@addSnapshotListener
                }
                
                val ideaList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Idea::class.java)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                userIdeas.value = ideaList
            }
        
        return userIdeas
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get category counts for categories screen
     */
    fun getCategoryCounts(): LiveData<Map<String, Int>> {
        val categoryCounts = MutableLiveData<Map<String, Int>>()
        
        ideasCollection
            .whereEqualTo("isApproved", true)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _error.value = "Failed to load category counts: ${exception.message}"
                    return@addSnapshotListener
                }
                
                val counts = mutableMapOf<String, Int>()
                snapshot?.documents?.forEach { doc ->
                    try {
                        val idea = doc.toObject(Idea::class.java)
                        idea?.let {
                            counts[it.category] = counts.getOrDefault(it.category, 0) + 1
                        }
                    } catch (e: Exception) {
                        // Skip invalid documents
                    }
                }
                
                categoryCounts.value = counts
            }
        
        return categoryCounts
    }
}
