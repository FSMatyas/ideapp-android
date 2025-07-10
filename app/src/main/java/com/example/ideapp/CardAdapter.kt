package com.example.ideapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CardAdapter(private val cards: List<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAdminComment: TextView = itemView.findViewById(R.id.tvAdminComment)
        val tvAdminCommentContent: TextView = itemView.findViewById(R.id.tvAdminCommentContent)
        val tvUserReply: TextView = itemView.findViewById(R.id.tvUserReply)
        val tvUserReplyContent: TextView = itemView.findViewById(R.id.tvUserReplyContent)
        val layoutReply: LinearLayout = itemView.findViewById(R.id.layoutReply)
        val etUserReply: EditText = itemView.findViewById(R.id.etUserReply)
        val btnSendReply: Button = itemView.findViewById(R.id.btnSendReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_idea_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.tvTitle.text = card.title
        holder.tvDescription.text = card.description

        // Show all messages
        val adminMsg = card.messages.find { it.sender == "admin" }
        if (adminMsg != null) {
            holder.tvAdminComment.visibility = View.VISIBLE
            holder.tvAdminCommentContent.visibility = View.VISIBLE
            holder.tvAdminCommentContent.text = adminMsg.text
        } else {
            holder.tvAdminComment.visibility = View.GONE
            holder.tvAdminCommentContent.visibility = View.GONE
        }
        val userMsg = card.messages.find { it.sender == card.creatorId }
        if (userMsg != null) {
            holder.tvUserReply.visibility = View.VISIBLE
            holder.tvUserReplyContent.visibility = View.VISIBLE
            holder.tvUserReplyContent.text = userMsg.text
            holder.layoutReply.visibility = View.GONE
        } else {
            holder.tvUserReply.visibility = View.GONE
            holder.tvUserReplyContent.visibility = View.GONE
            // Only show reply box if current user is creator
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && currentUser.uid == card.creatorId && adminMsg != null) {
                holder.layoutReply.visibility = View.VISIBLE
                holder.btnSendReply.setOnClickListener {
                    val replyText = holder.etUserReply.text.toString().trim()
                    if (replyText.isNotEmpty()) {
                        val db = FirebaseFirestore.getInstance()
                        val message = hashMapOf(
                            "sender" to currentUser.uid,
                            "text" to replyText,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("ideas")
                            .document(card.id)
                            .update("messages", com.google.firebase.firestore.FieldValue.arrayUnion(message))
                        holder.layoutReply.visibility = View.GONE
                    }
                }
            } else {
                holder.layoutReply.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = cards.size
}
