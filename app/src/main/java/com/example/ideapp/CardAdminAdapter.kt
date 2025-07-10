package com.example.ideapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CardAdminAdapter(
    private val cards: MutableList<Card>, // Changed to MutableList
    private val onApprove: (Card) -> Unit,
    private val onReject: (Card) -> Unit
) : RecyclerView.Adapter<CardAdminAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val cardRoot: View = itemView
        val btnSendMessage: Button = itemView.findViewById(R.id.btnSendMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_admin, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.tvTitle.text = card.title
        holder.tvDescription.text = card.description
        // Show submitter name/email
        holder.itemView.findViewById<TextView>(R.id.tvSubmitter)?.text =
            "Beküldő: ${card.submitterName} (${card.submitterEmail})"
        // Show sent date/time
        val sentDate = card.createdAt?.toDate()
        val sentDateStr = sentDate?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it)
        } ?: "-"
        holder.itemView.findViewById<TextView>(R.id.tvSentDateTime)?.text =
            "Beküldve: $sentDateStr"
        holder.tvStatus.text = card.status.replaceFirstChar { it.uppercase() }
        holder.tvStatus.visibility = View.VISIBLE

        // --- Show all messages (admin and user replies) with timestamp ---
        val layoutMessages = holder.itemView.findViewById<LinearLayout?>(R.id.layoutMessages)
        layoutMessages?.removeAllViews()
        card.messages.forEach { msg ->
            val tv = TextView(holder.itemView.context)
            val msgTime = msg.timestamp?.toDate()?.let {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it)
            } ?: "-"
            tv.text = if (msg.sender == "admin") {
                "Admin ($msgTime): ${msg.text}"
            } else {
                "Felhasználó ($msgTime): ${msg.text}"
            }
            tv.setPadding(0, 4, 0, 4)
            layoutMessages?.addView(tv)
        }

        // Status color
        when (card.status.uppercase()) {
            "PENDING" -> holder.cardRoot.setBackgroundColor(ContextCompat.getColor(holder.cardRoot.context, android.R.color.holo_orange_light))
            "APPROVED" -> holder.cardRoot.setBackgroundColor(ContextCompat.getColor(holder.cardRoot.context, android.R.color.holo_green_light))
            "REJECTED" -> holder.cardRoot.setBackgroundColor(ContextCompat.getColor(holder.cardRoot.context, android.R.color.holo_red_light))
            else -> holder.cardRoot.setBackgroundColor(Color.WHITE)
        }
        holder.btnApprove.visibility = if (card.status.equals("PENDING", ignoreCase = true)) View.VISIBLE else View.GONE
        holder.btnReject.visibility = if (card.status.equals("PENDING", ignoreCase = true)) View.VISIBLE else View.GONE
        holder.btnApprove.setOnClickListener { onApprove(card) }
        holder.btnReject.setOnClickListener {
            val context = holder.itemView.context
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Elutasítás megerősítése")
                .setMessage("Biztosan elutasítod ezt az ötletet?")
                .setPositiveButton("Igen") { _, _ -> onReject(card) }
                .setNegativeButton("Mégse", null)
                .show()
        }
        holder.btnSendMessage.setOnClickListener {
            val context = holder.itemView.context
            val input = EditText(context)
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Üzenet küldése")
                .setMessage("Írd be az üzenetet az ötlet beküldőjének!")
                .setView(input)
                .setPositiveButton("Küldés") { _, _ ->
                    val messageText = input.text.toString().trim()
                    if (messageText.isNotEmpty()) {
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val message = hashMapOf(
                            "sender" to "admin",
                            "text" to messageText,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("ideas")
                            .document(card.id)
                            .update("messages", com.google.firebase.firestore.FieldValue.arrayUnion(message))
                    }
                }
                .setNegativeButton("Mégse", null)
                .show()
        }
    }

    override fun getItemCount() = cards.size
}
