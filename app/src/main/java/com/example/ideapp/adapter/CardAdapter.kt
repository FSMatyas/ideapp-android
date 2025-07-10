package com.example.ideapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.Card
import com.example.ideapp.R

class CardAdapter(
    private val cards: List<Card>,
    private val onCardClick: (Card) -> Unit = {},
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvSubmitter: TextView = itemView.findViewById(R.id.tvSubmitter)
        val layoutMessages: LinearLayout = itemView.findViewById(R.id.layoutMessages)
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
        holder.tvSubmitter.text = "Beküldő: ${card.submitterName} (${card.submitterEmail})"
        holder.layoutMessages.removeAllViews()
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
            holder.layoutMessages.addView(tv)
        }
        holder.itemView.setOnClickListener { onCardClick(card) }
    }

    override fun getItemCount() = cards.size
}
