package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.AdminIdeaAdapter
import com.example.ideapp.data.Idea
import com.google.firebase.firestore.FirebaseFirestore

class AdminCompletedFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AdminIdeaAdapter
    private lateinit var btnPrev: View
    private lateinit var btnNext: View
    private lateinit var tvPage: android.widget.TextView
    private var completedIdeas: List<Idea> = emptyList()
    private var currentPage = 0
    private val pageSize = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewAdminCompleted)
        progressBar = view.findViewById(R.id.progressBarAdminCompleted)
        btnPrev = view.findViewById(R.id.btnPrevPageAdminCompleted)
        btnNext = view.findViewById(R.id.btnNextPageAdminCompleted)
        tvPage = view.findViewById(R.id.tvPageIndicatorAdminCompleted)
        adapter = AdminIdeaAdapter(emptyList(),
            onApprove = {},
            onReject = {},
            onSendMessage = { _, _ -> }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }
        btnNext.setOnClickListener {
            val maxPage = if (completedIdeas.isEmpty()) 0 else (completedIdeas.size - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updatePage()
            }
        }
        fetchCompletedIdeas()
    }

    private fun fetchCompletedIdeas() {
        progressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection("ideas")
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener { snapshot ->
                completedIdeas = snapshot.documents.mapNotNull { it.toObject(Idea::class.java)?.copy(id = it.id) }
                currentPage = 0
                updatePage()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hiba: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun updatePage() {
        val from = currentPage * pageSize
        val to = minOf(from + pageSize, completedIdeas.size)
        val pageIdeas = if (from < to) completedIdeas.subList(from, to) else emptyList()
        adapter.updateIdeas(pageIdeas)
        val maxPage = if (completedIdeas.isEmpty()) 1 else ((completedIdeas.size - 1) / pageSize) + 1
        tvPage.text = "${currentPage + 1}/$maxPage"
        btnPrev.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < maxPage - 1
    }
}
