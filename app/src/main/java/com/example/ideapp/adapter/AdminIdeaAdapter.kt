package com.example.ideapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.R
import com.example.ideapp.data.Idea

class AdminIdeaAdapter(
    private var ideas: List<Idea>,
    private val onApprove: (Idea) -> Unit,
    private val onReject: (Idea) -> Unit,
    private val onSendMessage: (Idea, String) -> Unit
) : RecyclerView.Adapter<AdminIdeaAdapter.AdminIdeaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminIdeaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_idea, parent, false)
        return AdminIdeaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminIdeaViewHolder, position: Int) {
        holder.bind(ideas[position])
    }

    override fun getItemCount(): Int = ideas.size

    fun updateIdeas(newIdeas: List<Idea>) {
        ideas = newIdeas
        notifyDataSetChanged()
    }

    inner class AdminIdeaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(idea: Idea) {
            itemView.findViewById<TextView>(R.id.tvIdeaText).text = idea.description
            itemView.findViewById<TextView>(R.id.tvSubmitterName).text = idea.submitterName
            itemView.findViewById<TextView>(R.id.tvSubmitterEmail).text = idea.submitterEmail
            itemView.findViewById<TextView>(R.id.tvStatus).text = when (idea.status) {
                com.example.ideapp.data.IdeaStatus.PENDING -> "Függőben"
                com.example.ideapp.data.IdeaStatus.APPROVED -> "Jóváhagyva"
                com.example.ideapp.data.IdeaStatus.REJECTED -> "Elutasítva"
                else -> idea.status.displayName
            }
            itemView.findViewById<Button>(R.id.btnApprove).setOnClickListener { onApprove(idea) }
            itemView.findViewById<Button>(R.id.btnReject).setOnClickListener { onReject(idea) }
            // Disable admin message UI
            val etMessage = itemView.findViewById<EditText>(R.id.etAdminMessage)
            val btnSend = itemView.findViewById<Button>(R.id.btnSendMessage)
            etMessage.visibility = View.GONE
            btnSend.visibility = View.GONE
            // btnSend.setOnClickListener { ... } is disabled
        }
    }
}
