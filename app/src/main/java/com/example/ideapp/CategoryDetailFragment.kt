package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.IdeaAdapter
import com.example.ideapp.data.Idea
import com.example.ideapp.viewmodel.IdeaViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class CategoryDetailFragment : Fragment() {

    private lateinit var tvCategoryIcon: TextView
    private lateinit var tvCategoryName: TextView
    private lateinit var tvCategoryCount: TextView
    private lateinit var recyclerViewIdeas: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var cvEmptyState: MaterialCardView

    private val viewModel: IdeaViewModel by viewModels()
    private lateinit var ideaAdapter: IdeaAdapter

    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryName = it.getString(ARG_CATEGORY_NAME) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupCategoryInfo()
        observeViewModel()
    }

    private fun initViews(view: View) {
        tvCategoryIcon = view.findViewById(R.id.tvCategoryIcon)
        tvCategoryName = view.findViewById(R.id.tvCategoryName)
        tvCategoryCount = view.findViewById(R.id.tvCategoryCount)
        recyclerViewIdeas = view.findViewById(R.id.recyclerViewIdeas)
        progressBar = view.findViewById(R.id.progressBar)
        cvEmptyState = view.findViewById(R.id.cvEmptyState)
    }

    private fun setupRecyclerView() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        ideaAdapter = IdeaAdapter(
            onIdeaClick = { idea -> onIdeaClick(idea) },
            blurUnapproved = false,
            currentUserEmail = currentUserEmail,
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
        recyclerViewIdeas.apply {
            adapter = ideaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupCategoryInfo() {
        // Set category icon and name
        val displayName = categoryName.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
        
        tvCategoryName.text = displayName
        
        // Set category icon based on category
        val iconText = getCategoryIcon(categoryName)
        tvCategoryIcon.text = iconText
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

    private fun observeViewModel() {
        // Observe ideas for this category
        viewModel.getIdeasByCategory(categoryName).observe(viewLifecycleOwner) { ideas ->
            ideaAdapter.submitList(ideas)
            updateIdeaCount(ideas.size)
            updateEmptyState(ideas.isEmpty())
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
    }

    private fun updateIdeaCount(count: Int) {
        tvCategoryCount.text = if (count == 1) {
            "$count idea"
        } else {
            "$count ideas"
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            cvEmptyState.visibility = View.VISIBLE
            recyclerViewIdeas.visibility = View.GONE
        } else {
            cvEmptyState.visibility = View.GONE
            recyclerViewIdeas.visibility = View.VISIBLE
        }
    }

    private fun onIdeaClick(idea: Idea) {
        // TODO: Navigate to idea detail view
        Toast.makeText(requireContext(), "Clicked on: ${idea.title}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "category_name"
        
        fun newInstance(categoryName: String): CategoryDetailFragment {
            return CategoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                }
            }
        }
    }
}
