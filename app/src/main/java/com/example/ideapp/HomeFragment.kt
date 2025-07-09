package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.IdeaAdapter
import com.example.ideapp.data.Idea
import com.example.ideapp.viewmodel.IdeaViewModel
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.min

class HomeFragment : Fragment() {
    
    private val viewModel: IdeaViewModel by viewModels()
    private lateinit var ideaAdapter: IdeaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var paginationContainer: ViewGroup

    private var currentPage = 0
    private val pageSize = 5
    private var allIdeas: List<Idea> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        paginationContainer = view.findViewById(R.id.paginationContainer)
        observeViewModel()
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewIdeas)
        progressBar = view.findViewById(R.id.progressBar)

        val submitButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubmitIdea)
        submitButton.setOnClickListener {
            val dialog = com.example.ideapp.SubmitIdeaDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "SubmitIdeaDialog")
        }

        val bulbIcon = view.findViewById<android.widget.ImageView>(R.id.bulbIcon)
        bulbIcon.setOnLongClickListener {
            // --- ADMIN LOGIN: Új Activity indítása ---
            // Ezt a blokkot bármikor eltávolíthatod vagy módosíthatod.
            val intent = android.content.Intent(requireContext(), AdminLoginActivity::class.java)
            startActivity(intent)
            true
        }
    }
    
    private fun setupRecyclerView() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        ideaAdapter = IdeaAdapter(
            onIdeaClick = { idea -> onIdeaClick(idea) },
            blurUnapproved = true,
            currentUserEmail = currentUserEmail,
            onSendReply = { idea, reply ->
                com.example.ideapp.repository.IdeaRepository.sendUserReply(idea.id, reply,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Válasz elküldve!", Toast.LENGTH_SHORT).show()
                        // Optionally refresh ideas here
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Hiba: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
        
        recyclerView.apply {
            adapter = ideaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeViewModel() {
        // Observe approved ideas
        viewModel.ideas.observe(viewLifecycleOwner) { ideas ->
            // Sort ideas by date (newest first)
            allIdeas = ideas.sortedByDescending { it.createdAt?.toDate() }
            updatePagedIdeas()
            updatePaginationButtons()
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
        
        // Observe submit success
        viewModel.submitSuccess.observe(viewLifecycleOwner) { success ->
            success?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSubmitSuccess()
            }
        }
    }
    
    private fun onIdeaClick(idea: Idea) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_idea_detail, null)

        // Set data into dialog views
        dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = idea.title
        dialogView.findViewById<TextView>(R.id.tvDialogCategory).text = getCategoryIcon(idea.category) + " " + idea.category.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        }
        dialogView.findViewById<TextView>(R.id.tvDialogSubmitter).text = "by ${idea.submitterName}"
        val dateText = idea.createdAt?.toDate()?.let {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(it)
        } ?: "Just now"
        dialogView.findViewById<TextView>(R.id.tvDialogDate).text = dateText
        dialogView.findViewById<TextView>(R.id.tvDialogDescription).text = idea.description

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        // Make dialog background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // Helper for category icon (moved from IdeaAdapter)
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

    private fun updatePagedIdeas() {
        val fromIndex = currentPage * pageSize
        val toIndex = min(fromIndex + pageSize, allIdeas.size)
        val pagedIdeas = if (fromIndex < toIndex) allIdeas.subList(fromIndex, toIndex) else emptyList()
        ideaAdapter.submitList(pagedIdeas)
    }

    private fun updatePaginationButtons() {
        paginationContainer.removeAllViews()
        val pageCount = (allIdeas.size + pageSize - 1) / pageSize
        for (i in 0 until pageCount) {
            val button = android.widget.TextView(requireContext())
            button.text = (i + 1).toString()
            button.setTextColor(android.graphics.Color.WHITE)
            button.textSize = 16f
            button.gravity = android.view.Gravity.CENTER
            button.setPadding(0, 0, 0, 0)
            val size = resources.displayMetrics.density * 36 // 36dp
            val params = ViewGroup.MarginLayoutParams(size.toInt(), size.toInt())
            params.setMargins(12, 0, 12, 0)
            button.layoutParams = params
            button.background = if (i == currentPage)
                requireContext().getDrawable(R.drawable.pagination_circle_selected)
            else
                requireContext().getDrawable(R.drawable.pagination_circle)
            button.setOnClickListener {
                currentPage = i
                updatePagedIdeas()
                updatePaginationButtons()
            }
            if (i == currentPage) {
                button.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            paginationContainer.addView(button)
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
