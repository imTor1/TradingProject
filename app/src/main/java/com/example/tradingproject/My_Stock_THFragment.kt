package com.example.tradingproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class My_Stock_THFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my__stock__t_h, container, false)

        val backbutton = view.findViewById<ImageView>(R.id.backbutton)

        backbutton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }
}