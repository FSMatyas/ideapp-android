package com.example.ideapp.adapter

import com.example.ideapp.onCompleteClicked

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
            // Show creation date/time
            val tvCreatedAt = itemView.findViewById<TextView>(R.id.tvCreatedAt)
            val createdAt = idea.createdAt?.toDate()
            tvCreatedAt.text = if (createdAt != null) {
                val sdf = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.getDefault())
                "Beküldés ideje: ${sdf.format(createdAt)}"
            } else {
                "Beküldés ideje: -"
            }
            val btnApprove = itemView.findViewById<Button>(R.id.btnApprove)
            val btnReject = itemView.findViewById<Button>(R.id.btnReject)
            val btnWork = itemView.findViewById<Button>(R.id.btnWork)
            val btnComplete = itemView.findViewById<Button>(R.id.btnComplete)
            btnComplete.visibility = View.GONE
            if (idea.status == com.example.ideapp.data.IdeaStatus.PENDING) {
                btnApprove.visibility = View.VISIBLE
                btnReject.visibility = View.VISIBLE
                btnWork.visibility = View.GONE
                btnComplete.visibility = View.GONE
                btnApprove.setOnClickListener { onApprove(idea) }
                btnReject.setOnClickListener { onReject(idea) }
            } else if (idea.status == com.example.ideapp.data.IdeaStatus.APPROVED) {
                btnApprove.visibility = View.GONE
                btnReject.visibility = View.GONE
                btnWork.visibility = View.VISIBLE
                btnComplete.visibility = View.GONE
                btnWork.setOnClickListener { 
                    (itemView.context as? android.app.Activity)?.let { activity ->
                        if (activity is com.example.ideapp.AdminNavActivity) {
                            (activity.supportFragmentManager.findFragmentById(R.id.admin_nav_host_fragment) as? com.example.ideapp.AdminPanelFragment)?.onWorkClicked(idea)
                        }
                    }
                }
            } else if (idea.status == com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT) {
                btnApprove.visibility = View.GONE
                btnReject.visibility = View.GONE
                btnWork.visibility = View.GONE
                btnComplete.visibility = View.VISIBLE
                btnComplete.setOnClickListener {
                    (itemView.context as? android.app.Activity)?.let { activity ->
                        if (activity is com.example.ideapp.AdminNavActivity) {
                            (activity.supportFragmentManager.findFragmentById(R.id.admin_nav_host_fragment) as? com.example.ideapp.AdminWorkFragment)?.onCompleteClicked(idea)
                        }
                    }
                }
            } else {
                btnApprove.visibility = View.GONE
                btnReject.visibility = View.GONE
                btnWork.visibility = View.GONE
                btnComplete.visibility = View.GONE
            }
            // Enable admin message UI
            val etMessage = itemView.findViewById<EditText>(R.id.etAdminMessage)
            val btnSend = itemView.findViewById<Button>(R.id.btnSendMessage)
            etMessage.visibility = View.VISIBLE
            btnSend.visibility = View.VISIBLE
            btnSend.setOnClickListener {
                val msg = etMessage.text.toString().trim()
                if (msg.isNotEmpty()) {
                    onSendMessage(idea, msg)
                    etMessage.text.clear()
                }
            }
        }
    }
}
