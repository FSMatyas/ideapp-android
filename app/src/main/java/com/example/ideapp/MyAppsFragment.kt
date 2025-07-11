package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.IdeaAdapter
import com.example.ideapp.data.Idea
import android.widget.LinearLayout
import android.graphics.Color
import kotlin.math.min
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class MyAppsFragment : Fragment() {
    private lateinit var completedAdapter: IdeaAdapter
    private lateinit var inProgressAdapter: IdeaAdapter
    private lateinit var recyclerViewCompleted: RecyclerView
    private lateinit var recyclerViewInProgress: RecyclerView
    private lateinit var paginationCompleted: LinearLayout
    private lateinit var paginationInProgress: LinearLayout
    private var completedApps: List<Idea> = emptyList()
    private var completedPage = 0
    private var inProgressPage = 0
    private val pageSize = 5
    private var allInProgressIdeas: List<Idea> = emptyList()
    private var inProgressListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewCompleted = view.findViewById(R.id.recyclerViewCompletedApps)
        recyclerViewInProgress = view.findViewById(R.id.recyclerViewInProgress)
        paginationCompleted = view.findViewById(R.id.paginationCompletedContainer)
        paginationInProgress = view.findViewById(R.id.paginationInProgressContainer)

        completedAdapter = IdeaAdapter(
            onIdeaClick = { idea -> showIdeaDialog(idea) },
            blurUnapproved = false,
            currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
            onSendReply = { idea, reply ->
                com.example.ideapp.repository.IdeaRepository.sendUserReply(idea.id, reply,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Válasz elküldve!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Hiba: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
        inProgressAdapter = IdeaAdapter(
            onIdeaClick = { idea -> showIdeaDialog(idea) },
            blurUnapproved = false,
            currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
            onSendReply = { idea, reply ->
                com.example.ideapp.repository.IdeaRepository.sendUserReply(idea.id, reply,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Válasz elküldve!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Hiba: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
        recyclerViewInProgress.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewInProgress.adapter = inProgressAdapter

        // Listen to live in-progress ideas for the current user
        val currentUserId = com.example.ideapp.util.DeviceIdUtil.getDeviceId(requireContext())
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        inProgressListener = db.collection("ideas")
            .whereEqualTo("creatorId", currentUserId)
            .whereEqualTo("status", com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT.name)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                allInProgressIdeas = snapshot?.documents?.mapNotNull { it.toObject(com.example.ideapp.data.Idea::class.java) } ?: emptyList()
                updateInProgressPage()
                updateInProgressPagination()
                // Update the in-progress count text
                val tvInProgressCount = view.findViewById<android.widget.TextView>(R.id.tvInProgressCount)
                tvInProgressCount.text = "${allInProgressIdeas.size} in progress"
                // Hide empty state card if there are any apps
                val emptyStateCard = view.findViewById<View>(R.id.emptyStateCard)
                if (allInProgressIdeas.isNotEmpty()) {
                    emptyStateCard.visibility = View.GONE
                } else {
                    emptyStateCard.visibility = View.VISIBLE
                }
            }
        // Completed apps demo (keep as before)
        val exampleCompleted = Idea(
            id = "1",
            title = "Simple Password Generator",
            description = "Generate secure passwords with customizable length and character sets.",
            category = "Utility",
            submitterEmail = "demo@example.com",
            submitterName = "Demo User",
            status = com.example.ideapp.data.IdeaStatus.COMPLETED,
            upvotes = 47,
            estimatedDevelopmentDays = 2,
            estimatedPrice = 0.0,
            createdAt = null
        )
        completedApps = listOf(exampleCompleted)
        updateCompletedPage()
        updateCompletedPagination()
    }

    override fun onResume() {
        super.onResume()
        updateInProgressPage()
        updateInProgressPagination()
    }

    private fun updateCompletedPage() {
        val from = completedPage * pageSize
        val to = min(from + pageSize, completedApps.size)
        completedAdapter.submitList(if (from < to) completedApps.subList(from, to) else emptyList())
    }

    private fun updateInProgressPage() {
        val from = inProgressPage * pageSize
        val to = min(from + pageSize, allInProgressIdeas.size)
        inProgressAdapter.submitList(if (from < to) allInProgressIdeas.subList(from, to) else emptyList())
    }
    override fun onDestroyView() {
        super.onDestroyView()
        inProgressListener?.remove()
    }

    private fun updateCompletedPagination() {
        paginationCompleted.removeAllViews()
        val pageCount = (completedApps.size + pageSize - 1) / pageSize
        for (i in 0 until pageCount) {
            val button = android.widget.TextView(requireContext())
            button.text = (i + 1).toString()
            button.setTextColor(android.graphics.Color.WHITE)
            button.textSize = 16f
            button.gravity = android.view.Gravity.CENTER
            button.setPadding(0, 0, 0, 0)
            val size = resources.displayMetrics.density * 36 // 36dp
            val params = LinearLayout.LayoutParams(size.toInt(), size.toInt())
            params.setMargins(12, 0, 12, 0)
            button.layoutParams = params
            button.background = if (i == completedPage)
                requireContext().getDrawable(R.drawable.pagination_circle_selected)
            else
                requireContext().getDrawable(R.drawable.pagination_circle)
            button.setOnClickListener {
                completedPage = i
                updateCompletedPage()
                updateCompletedPagination()
            }
            if (i == completedPage) {
                button.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            paginationCompleted.addView(button)
        }
    }

    private fun updateInProgressPagination() {
        paginationInProgress.removeAllViews()
        val pageCount = (allInProgressIdeas.size + pageSize - 1) / pageSize
        for (i in 0 until pageCount) {
            val button = android.widget.TextView(requireContext())
            button.text = (i + 1).toString()
            button.setTextColor(android.graphics.Color.WHITE)
            button.textSize = 16f
            button.gravity = android.view.Gravity.CENTER
            button.setPadding(0, 0, 0, 0)
            val size = resources.displayMetrics.density * 36 // 36dp
            val params = LinearLayout.LayoutParams(size.toInt(), size.toInt())
            params.setMargins(12, 0, 12, 0)
            button.layoutParams = params
            button.background = if (i == inProgressPage)
                requireContext().getDrawable(R.drawable.pagination_circle_selected)
            else
                requireContext().getDrawable(R.drawable.pagination_circle)
            button.setOnClickListener {
                inProgressPage = i
                updateInProgressPage()
                updateInProgressPagination()
            }
            if (i == inProgressPage) {
                button.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            paginationInProgress.addView(button)
        }
    }
    private fun showIdeaDialog(idea: Idea) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_idea_detail, null)
        dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle).text = idea.title
        dialogView.findViewById<android.widget.TextView>(R.id.tvDialogCategory).text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        }
        dialogView.findViewById<android.widget.TextView>(R.id.tvDialogSubmitter).text = "by ${idea.submitterName}"
        val dateText = idea.createdAt?.toDate()?.let {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(it)
        } ?: "Just now"
        dialogView.findViewById<android.widget.TextView>(R.id.tvDialogDate).text = dateText
        dialogView.findViewById<android.widget.TextView>(R.id.tvDialogDescription).text = idea.description

        // Show messages in the dialog at the bottom
        val layoutDialogMessages = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutDialogMessages)
        layoutDialogMessages?.removeAllViews()
        if (idea.messages.isNotEmpty()) {
            idea.messages.forEach { msg ->
                val tv = android.widget.TextView(requireContext())
                tv.text = if (msg.sender == "admin") {
                    "Admin: ${msg.text}"
                } else {
                    "Te: ${msg.text}"
                }
                tv.setPadding(0, 4, 0, 4)
                layoutDialogMessages?.addView(tv)
            }
        }

        // --- Reply box logic ---
        val layoutReply = dialogView.findViewById<android.widget.LinearLayout>(R.id.layoutReplyDialog)
        val etUserReply = dialogView.findViewById<android.widget.EditText>(R.id.etUserReplyDialog)
        val btnSendReply = dialogView.findViewById<android.widget.Button>(R.id.btnSendReplyDialog)
        layoutReply?.visibility = View.GONE
        // Only show if current user is creator and there is at least one admin message
        val deviceId = com.example.ideapp.util.DeviceIdUtil.getDeviceId(requireContext())
        val hasAdminMsg = idea.messages.any { it.sender == "admin" }
        if (deviceId == idea.creatorId && hasAdminMsg) {
            layoutReply?.visibility = View.VISIBLE
            btnSendReply?.setOnClickListener {
                val replyText = etUserReply?.text?.toString()?.trim()
                if (!replyText.isNullOrEmpty()) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val message = hashMapOf(
                        "sender" to deviceId,
                        "text" to replyText,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    db.collection("ideas")
                        .document(idea.id)
                        .update("messages", com.google.firebase.firestore.FieldValue.arrayUnion(message))
                    layoutReply.visibility = View.GONE
                    android.widget.Toast.makeText(requireContext(), "Válasz elküldve!", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "productivity" -> "\uD83D\uDCBC" // 💼
            "health" -> "\uD83C\uDFE5" // 🏥
            "education" -> "\uD83C\uDF93" // 🎓
            "social" -> "\uD83D\uDC65" // 👥
            "entertainment" -> "\uD83C\uDFAC" // 🎬
            "utility" -> "\uD83D\uDD27" // 🔧
            "finance" -> "\uD83D\uDCB0" // 💰
            "other" -> "\uD83D\uDCF1" // 📱
            else -> "\uD83D\uDCF1" // 📱
        }
    }

    private fun loadAllIdeas(): List<Idea> {
        // TODO: Replace with real data loading from ViewModel or Firebase
        return emptyList()
    }

    companion object {
        fun newInstance() = MyAppsFragment()
    }
}
