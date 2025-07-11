package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ideapp.adapter.AdminIdeaAdapter
import com.example.ideapp.viewmodel.AdminPanelViewModel
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import androidx.navigation.fragment.findNavController

class AdminPanelFragment : Fragment() {
    private val viewModel: AdminPanelViewModel by viewModels()
    private lateinit var adapter: AdminIdeaAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var logoutButton: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPage: android.widget.TextView
    private var allIdeas: List<com.example.ideapp.data.Idea> = emptyList()
    private var currentPage = 0
    private val pageSize = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adminEmail = "sandorfulop44@gmail.com"
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.email != adminEmail) {
            Toast.makeText(requireContext(), "Nincs admin jogosultság!", Toast.LENGTH_LONG).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }
        recyclerView = view.findViewById(R.id.recyclerViewAdminIdeas)
        progressBar = view.findViewById(R.id.progressBarAdmin)
        logoutButton = view.findViewById(R.id.btnLogout)
        btnPrev = view.findViewById(R.id.btnPrevPageAdminHome)
        btnNext = view.findViewById(R.id.btnNextPageAdminHome)
        tvPage = view.findViewById(R.id.tvPageIndicatorAdminHome)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Kijelentkezve", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(requireContext(), com.example.ideapp.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        adapter = AdminIdeaAdapter(emptyList(),
            onApprove = { idea ->
                viewModel.updateIdeaStatus(idea.id, "approved")
            },
            onReject = { idea ->
                viewModel.updateIdeaStatus(idea.id, "rejected")
            },
            onSendMessage = { idea, msg ->
                viewModel.sendAdminMessage(idea.id, msg)
            }
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
            val maxPage = if (allIdeas.isEmpty()) 0 else (allIdeas.size - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updatePage()
            }
        }
        observeViewModel()
        viewModel.fetchIdeas()
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

    private fun observeViewModel() {
        viewModel.ideas.observe(viewLifecycleOwner, Observer { ideas ->
            allIdeas = ideas
            currentPage = 0
            updatePage()
            progressBar.visibility = View.GONE
        })
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        })
        viewModel.messageSent.observe(viewLifecycleOwner, Observer { sent ->
            if (sent == true) {
                Toast.makeText(requireContext(), "Üzenet elküldve!", Toast.LENGTH_SHORT).show()
                viewModel.fetchIdeas()
            }
        })
    }
}
