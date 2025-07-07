package com.example.ideapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ideapp.data.Idea
import com.example.ideapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class IdeaViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    val ideas: LiveData<List<Idea>> = repository.ideas
    val isLoading: LiveData<Boolean> = repository.isLoading
    val error: LiveData<String?> = repository.error
    
    // Additional LiveData for UI state
    private val _submitSuccess = MutableLiveData<String?>()
    val submitSuccess: LiveData<String?> = _submitSuccess
    
    // Filtered ideas for approved items
    val approvedIdeas: LiveData<List<Idea>> = repository.ideas

    /**
     * Get ideas by category
     */
    fun getIdeasByCategory(category: String): LiveData<List<Idea>> {
        return repository.getIdeasByCategory(category)
    }

    /**
     * Get ideas by submitter email (for My Apps section)
     */
    fun getIdeasBySubmitter(email: String): LiveData<List<Idea>> {
        return repository.getIdeasBySubmitter(email)
    }

    /**
     * Submit a new idea
     */
    fun submitIdea(idea: Idea, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.submitIdea(idea)
            result.fold(
                onSuccess = { _ ->
                    _submitSuccess.value = "Idea submitted successfully!"
                    onComplete(true, "Idea submitted successfully!")
                },
                onFailure = { exception ->
                    onComplete(false, exception.message)
                }
            )
        }
    }

    /**
     * Get category counts for categories screen
     */
    fun getCategoryCounts(): LiveData<Map<String, Int>> {
        return repository.getCategoryCounts()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        repository.clearError()
    }
    
    /**
     * Clear submit success message
     */
    fun clearSubmitSuccess() {
        _submitSuccess.value = null
    }
}
