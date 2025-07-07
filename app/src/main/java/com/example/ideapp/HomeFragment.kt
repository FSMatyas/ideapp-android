package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.IdeaAdapter
import com.example.ideapp.data.Idea
import com.example.ideapp.viewmodel.IdeaViewModel
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {
    
    private val viewModel: IdeaViewModel by viewModels()
    private lateinit var ideaAdapter: IdeaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

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
        observeViewModel()
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewIdeas)
        progressBar = view.findViewById(R.id.progressBar)
        
        // Set up the submit idea button
        val submitButton = view.findViewById<MaterialButton>(R.id.btnSubmitIdea)
        submitButton.setOnClickListener {
            // Open the submission dialog
            val dialog = SubmitIdeaDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "SubmitIdeaDialog")
        }
    }
    
    private fun setupRecyclerView() {
        ideaAdapter = IdeaAdapter { idea ->
            onIdeaClick(idea)
        }
        
        recyclerView.apply {
            adapter = ideaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeViewModel() {
        // Observe approved ideas
        viewModel.ideas.observe(viewLifecycleOwner) { ideas ->
            ideaAdapter.submitList(ideas)
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
        // TODO: Navigate to idea detail view
        Toast.makeText(requireContext(), "Clicked on: ${idea.title}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
