package com.example.ideapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data model for app ideas submitted by users
 */
data class Idea(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val submitterEmail: String = "",
    val submitterName: String = "",
    val status: IdeaStatus = IdeaStatus.PENDING,
    val isApproved: Boolean = false,
    val isFirstIdeaByUser: Boolean = false, // Auto-approve first idea
    val comments: List<Comment> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val estimatedDevelopmentDays: Int = 0,
    val estimatedPrice: Double = 0.0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    val adminNotes: String = ""
)

/**
 * Status of an idea in the development pipeline
 */
enum class IdeaStatus(val displayName: String, val color: String) {
    PENDING("Pending Review", "#FFA726"), // Orange
    APPROVED("Approved", "#66BB6A"), // Green
    IN_DEVELOPMENT("In Development", "#42A5F5"), // Blue
    TESTING("Testing", "#AB47BC"), // Purple
    COMPLETED("Completed", "#26A69A"), // Teal
    REJECTED("Rejected", "#EF5350") // Red
}

/**
 * Comment on an idea
 */
data class Comment(
    val id: String = "",
    val text: String = "",
    val authorName: String = "",
    val authorEmail: String = "",
    val isFromDeveloper: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

/**
 * Category for organizing ideas
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val color: String = "",
    val ideaCount: Int = 0
)
