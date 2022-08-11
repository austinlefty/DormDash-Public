package com.dormdash.android.driver.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.databinding.FragmentUpdateDriverStatusBinding


class UpdateDriverStatusFragment : Fragment() {
    private var fragmentUpdateDriverStatusBinding : FragmentUpdateDriverStatusBinding? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUpdateDriverStatusBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        fragmentUpdateDriverStatusBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

}