package com.example.ideapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ideapp.R

class AboutUsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_about_us, container, false)
        val facebookLogo = view.findViewById<ImageView>(R.id.facebook_logo)
        val facebookLink = view.findViewById<TextView>(R.id.facebook_link)
        val url = "https://www.facebook.com/profile.php?id=61578098383672"
        val openFacebook = View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        facebookLogo.setOnClickListener(openFacebook)
        facebookLink.setOnClickListener(openFacebook)
        return view
    }
}
