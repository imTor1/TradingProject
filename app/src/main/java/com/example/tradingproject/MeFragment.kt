package com.example.tradingproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logout = view.findViewById<LinearLayout>(R.id.logout_button)
        val profile = view.findViewById<LinearLayout>(R.id.profile)

        profile.setOnClickListener{
            findNavController().navigate(R.id.nav_profile)
        }

        logout.setOnClickListener {
            val intent = Intent(requireContext(), LoginPage::class.java)
            startActivity(intent)
        }

    }


}