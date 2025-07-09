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
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Kijelentkezve", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_adminPanelFragment_to_homeFragment)
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
        observeViewModel()
        viewModel.fetchIdeas()
    }

    private fun observeViewModel() {
        viewModel.ideas.observe(viewLifecycleOwner, Observer { ideas ->
            adapter.updateIdeas(ideas)
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
