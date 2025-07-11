package com.example.ideapp.adapter

import android.graphics.RenderEffect
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.R
import com.example.ideapp.data.Idea
import com.example.ideapp.data.IdeaStatus
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class IdeaAdapter(
    private val onIdeaClick: (Idea) -> Unit,
    private val blurUnapproved: Boolean = false, // Only blur on landing page
    private val currentUserEmail: String,
    private val onSendReply: (Idea, String) -> Unit,
    private val onStatusChanged: (() -> Unit)? = null // Add callback for status change
) : ListAdapter<Idea, IdeaAdapter.IdeaViewHolder>(IdeaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_idea_card, parent, false)
        return IdeaViewHolder(view)
    }

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IdeaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val contentLayout: LinearLayout = itemView.findViewById(R.id.contentLayout)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvSubmitter: TextView = itemView.findViewById(R.id.tvSubmitter)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(idea: Idea) {
            tvTitle.text = if (!idea.title.isNullOrBlank()) idea.title else itemView.context.getString(R.string.untitled_idea)
            tvDescription.text = if (!idea.description.isNullOrBlank()) idea.description else itemView.context.getString(R.string.no_description)
            tvCategory.text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
            }
            tvSubmitter.text = if (!idea.submitterName.isNullOrBlank()) "by ${idea.submitterName}" else itemView.context.getString(R.string.unknown_submitter)

            // Format date
            idea.createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = formatter.format(date)
            } ?: run {
                tvDate.text = "Just now"
            }

            // Blur or hide content for pending items
            if (idea.status == IdeaStatus.PENDING) {
                // For API 31+, blur the content area
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    contentLayout.setRenderEffect(RenderEffect.createBlurEffect(16f, 16f, Shader.TileMode.CLAMP))
                } else {
                    // For lower APIs, hide the content or show a placeholder
                    tvDescription.text = itemView.context.getString(R.string.pending_approval)
                    tvCategory.text = ""
                    tvSubmitter.text = ""
                }
            } else {
                // Remove blur if not pending
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    contentLayout.setRenderEffect(null)
                }
            }

            // --- Show latest admin and user messages in card preview ---
            val tvAdminComment = itemView.findViewById<TextView>(R.id.tvAdminComment)
            val tvAdminCommentContent = itemView.findViewById<TextView>(R.id.tvAdminCommentContent)
            val tvUserReply = itemView.findViewById<TextView>(R.id.tvUserReply)
            val tvUserReplyContent = itemView.findViewById<TextView>(R.id.tvUserReplyContent)
            val layoutReply = itemView.findViewById<LinearLayout>(R.id.layoutReply)
            val etUserReply = itemView.findViewById<EditText>(R.id.etUserReply)
            val btnSendReply = itemView.findViewById<Button>(R.id.btnSendReply)

            // Show last admin message if present
            val lastAdminMsg = idea.messages.lastOrNull { it.sender == "admin" }
            if (lastAdminMsg != null) {
                tvAdminComment.visibility = View.VISIBLE
                tvAdminCommentContent.visibility = View.VISIBLE
                tvAdminCommentContent.text = lastAdminMsg.text
            } else {
                tvAdminComment.visibility = View.GONE
                tvAdminCommentContent.visibility = View.GONE
            }

            // Show last user reply if present
            val lastUserMsg = idea.messages.lastOrNull { it.sender != "admin" }
            if (lastUserMsg != null) {
                tvUserReply.visibility = View.VISIBLE
                tvUserReplyContent.visibility = View.VISIBLE
                tvUserReplyContent.text = lastUserMsg.text
            } else {
                tvUserReply.visibility = View.GONE
                tvUserReplyContent.visibility = View.GONE
            }

            layoutReply.visibility = View.GONE

            // Envelope icon and counter logic
            val layoutMessageIcon = itemView.findViewById<LinearLayout>(R.id.layoutMessageIcon)
            val ivEnvelope = itemView.findViewById<android.widget.ImageView>(R.id.ivEnvelope)
            val tvMessageCount = itemView.findViewById<TextView>(R.id.tvMessageCount)
            val adminMessages = idea.messages.filter { it.sender == "admin" }
            if (adminMessages.isNotEmpty()) {
                ivEnvelope.visibility = View.VISIBLE
                tvMessageCount.visibility = View.VISIBLE
                tvMessageCount.text = idea.messages.size.toString()
            } else {
                ivEnvelope.visibility = View.GONE
                tvMessageCount.visibility = View.GONE
            }

            // Work button removed from user layout; nothing to do here

            // Set click listener: allow all statuses to open detail dialog
            cardView.setOnClickListener {
                onIdeaClick(idea)
            }
        }

        private fun getCategoryIcon(category: String): String {
            return when (category.lowercase()) {
                "productivity" -> "💼"
                "health" -> "🏥"
                "education" -> "🎓"
                "social" -> "👥"
                "entertainment" -> "🎬"
                "utility" -> "🔧"
                "finance" -> "💰"
                "other" -> "📱"
                else -> "📱"
            }
        }

        private fun getStatusColorResource(status: IdeaStatus): Int {
            return when (status) {
                IdeaStatus.PENDING -> R.color.status_pending
                IdeaStatus.APPROVED -> R.color.status_approved
                IdeaStatus.IN_DEVELOPMENT -> R.color.status_in_development
                IdeaStatus.TESTING -> R.color.status_testing
                IdeaStatus.COMPLETED -> R.color.status_completed
                IdeaStatus.REJECTED -> R.color.status_rejected
            }
        }
    }

    private class IdeaDiffCallback : DiffUtil.ItemCallback<Idea>() {
        override fun areItemsTheSame(oldItem: Idea, newItem: Idea): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Idea, newItem: Idea): Boolean {
            return oldItem == newItem
        }
    }
}
