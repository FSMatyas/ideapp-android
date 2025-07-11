package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.AdminIdeaAdapter
import com.example.ideapp.data.Idea
import com.google.firebase.firestore.FirebaseFirestore

class AdminWorkFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminIdeaAdapter
    private lateinit var btnPrev: android.widget.Button
    private lateinit var btnNext: android.widget.Button
    private lateinit var tvPage: android.widget.TextView
    private var allIdeas: List<Idea> = emptyList()
    private var currentPage = 0
    private val pageSize = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_work, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewAdminWork)
        btnPrev = view.findViewById(R.id.btnPrevPageAdminWork)
        btnNext = view.findViewById(R.id.btnNextPageAdminWork)
        tvPage = view.findViewById(R.id.tvPageIndicatorAdminWork)
        adapter = AdminIdeaAdapter(emptyList(), onApprove = {}, onReject = {}, onSendMessage = { _, _ -> })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }
        btnNext.setOnClickListener {
            val maxPage = if (allIdeas.isEmpty()) 0 else (allIdeas.size - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updatePage()
            }
        }
        fetchInDevelopmentIdeas()
    }

    private fun fetchInDevelopmentIdeas() {
        FirebaseFirestore.getInstance().collection("ideas")
            .whereEqualTo("status", "IN_DEVELOPMENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Hiba: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                allIdeas = snapshot?.documents
                    ?.mapNotNull { it.toObject(com.example.ideapp.data.Idea::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                currentPage = 0
                updatePage()
            }
    }

    private fun updatePage() {
        val from = currentPage * pageSize
        val to = minOf(from + pageSize, allIdeas.size)
        val pageIdeas = if (from < to) allIdeas.subList(from, to) else emptyList()
        adapter.updateIdeas(pageIdeas)
        val maxPage = if (allIdeas.isEmpty()) 1 else ((allIdeas.size - 1) / pageSize) + 1
        tvPage.text = "${currentPage + 1}/$maxPage"
        btnPrev.isEnabled = currentPage > 0
        btnNext.isEnabled = currentPage < maxPage - 1
    }
}
