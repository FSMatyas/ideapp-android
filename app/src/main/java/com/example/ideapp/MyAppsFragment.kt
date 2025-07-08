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

class MyAppsFragment : Fragment() {
    private lateinit var completedAdapter: IdeaAdapter
    private lateinit var inProgressAdapter: IdeaAdapter
    private lateinit var recyclerViewCompleted: RecyclerView
    private lateinit var recyclerViewInProgress: RecyclerView
    private lateinit var paginationCompleted: LinearLayout
    private lateinit var paginationInProgress: LinearLayout
    private var completedApps: List<Idea> = emptyList()
    private var inProgressApps: List<Idea> = emptyList()
    private var completedPage = 0
    private var inProgressPage = 0
    private val pageSize = 5

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
        val emptyStateCard = view.findViewById<View>(R.id.emptyStateCard)

        completedAdapter = IdeaAdapter { idea -> showIdeaDialog(idea) }
        inProgressAdapter = IdeaAdapter { idea -> showIdeaDialog(idea) }
        recyclerViewCompleted.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewInProgress.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCompleted.adapter = completedAdapter
        recyclerViewInProgress.adapter = inProgressAdapter

        // Example completed app card for prototype/demo
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
            createdAt = null // You can set a Timestamp if needed
        )
        // Example in-progress cards for pagination test
        val exampleInProgress1 = Idea(
            id = "2",
            title = "Personal Recipe Organizer",
            description = "Organize family recipes with photos, ingredients, and cooking time.",
            category = "Productivity",
            submitterEmail = "client1@example.com",
            submitterName = "Client One",
            status = com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT,
            upvotes = 5,
            estimatedDevelopmentDays = 3,
            estimatedPrice = 0.0,
            createdAt = null
        )
        val exampleInProgress2 = Idea(
            id = "3",
            title = "Quick Daily Journal",
            description = "A simple journal app for daily thoughts and mood tracking.",
            category = "Health",
            submitterEmail = "client2@example.com",
            submitterName = "Client Two",
            status = com.example.ideapp.data.IdeaStatus.IN_DEVELOPMENT,
            upvotes = 2,
            estimatedDevelopmentDays = 4,
            estimatedPrice = 0.0,
            createdAt = null
        )
        completedApps = listOf(exampleCompleted)
        inProgressApps = listOf(exampleInProgress1, exampleInProgress2)
        updateCompletedPage()
        updateInProgressPage()
        updateCompletedPagination()
        updateInProgressPagination()

        // Hide empty state card if there are any apps
        if (completedApps.isNotEmpty() || inProgressApps.isNotEmpty()) {
            emptyStateCard.visibility = View.GONE
        } else {
            emptyStateCard.visibility = View.VISIBLE
        }
    }

    private fun updateCompletedPage() {
        val from = completedPage * pageSize
        val to = min(from + pageSize, completedApps.size)
        completedAdapter.submitList(if (from < to) completedApps.subList(from, to) else emptyList())
    }
    private fun updateInProgressPage() {
        val from = inProgressPage * pageSize
        val to = min(from + pageSize, inProgressApps.size)
        inProgressAdapter.submitList(if (from < to) inProgressApps.subList(from, to) else emptyList())
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
        val pageCount = (inProgressApps.size + pageSize - 1) / pageSize
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
