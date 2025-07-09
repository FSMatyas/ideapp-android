package com.example.ideapp.adapter

import android.graphics.RenderEffect
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onIdeaClick: (Idea) -> Unit
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
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
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

            // Blur or dim card if not approved
            if (idea.status != IdeaStatus.APPROVED) {
                cardView.alpha = 0.4f
                tvTitle.alpha = 0.6f
                tvDescription.alpha = 0.6f
                tvCategory.alpha = 0.6f
                tvSubmitter.alpha = 0.6f
                tvDate.alpha = 0.6f
                chipStatus.alpha = 0.6f
                tvUpvotes.alpha = 0.6f
                cardView.isClickable = false
                // Apply blur effect (API 31+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    cardView.setRenderEffect(RenderEffect.createBlurEffect(12f, 12f, Shader.TileMode.CLAMP))
                }
            } else {
                cardView.alpha = 1.0f
                tvTitle.alpha = 1.0f
                tvDescription.alpha = 1.0f
                tvCategory.alpha = 1.0f
                tvSubmitter.alpha = 1.0f
                tvDate.alpha = 1.0f
                chipStatus.alpha = 1.0f
                tvUpvotes.alpha = 1.0f
                cardView.isClickable = true
                // Remove blur effect
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    cardView.setRenderEffect(null)
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
