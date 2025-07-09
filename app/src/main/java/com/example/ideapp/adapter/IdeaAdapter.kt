package com.example.ideapp.adapter

import android.graphics.RenderEffect
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val blurUnapproved: Boolean = false // Only blur on landing page
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
        private val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        private val tvUpvotes: TextView = itemView.findViewById(R.id.tvUpvotes)

        fun bind(idea: Idea) {
            tvTitle.text = idea.title
            tvDescription.text = idea.description
            tvCategory.text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
            }
            tvSubmitter.text = "by ${idea.submitterName}"

            // Format date
            idea.createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = formatter.format(date)
            } ?: run {
                tvDate.text = "Just now"
            }

            // Set status chip
            chipStatus.text = idea.status.displayName
            chipStatus.setChipBackgroundColorResource(getStatusColorResource(idea.status))

            // Set upvotes
            tvUpvotes.text = idea.upvotes.toString()

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
                    tvDate.text = ""
                }
            } else {
                // Remove blur/hiding for approved items
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    contentLayout.setRenderEffect(null)
                }
                // Restore normal content for lower APIs
                tvDescription.text = idea.description
                tvCategory.text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                }
                tvSubmitter.text = "by ${idea.submitterName}"
                idea.createdAt?.let { timestamp ->
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    tvDate.text = formatter.format(date)
                } ?: run {
                    tvDate.text = "Just now"
                }
            }

            // Set click listener
            cardView.setOnClickListener {
                if (idea.status == IdeaStatus.APPROVED) {
                    onIdeaClick(idea)
                }
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
