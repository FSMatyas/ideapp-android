package com.example.ideapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class AdminPanelFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Admin Panel"
        textView.textSize = 24f
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        return textView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adminEmail = "sandorfulop44@gmail.com"
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.email == adminEmail) {
            // admin funkciók
            // Például: Toast.makeText(requireContext(), "Admin bejelentkezve", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Nincs admin jogosultság!", Toast.LENGTH_LONG).show()
            // Itt akár vissza is navigálhatsz, vagy elrejtheted az admin funkciókat
        }
    }
}
