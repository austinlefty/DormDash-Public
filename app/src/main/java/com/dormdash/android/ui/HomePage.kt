package com.dormdash.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentHomePageBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.firebase.auth.FirebaseAuth


class HomePage : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private var fragmentHomePageBinding: FragmentHomePageBinding? = null
    private val viewModel : SharedCartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentHomePageBinding.inflate(inflater, container, false)
        fragmentHomePageBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.root_homepage_signout_button).setOnClickListener(this)
        firebaseAuth = FirebaseAuth.getInstance()
        fragmentHomePageBinding!!.selectCustomerHomeButton.setOnClickListener(this)
        fragmentHomePageBinding!!.driverButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.driver_button -> navController.navigate(R.id.action_homePage_to_driverHomePageFragment)
            R.id.select_Customer_HomeButton -> navController.navigate(R.id.action_homePage_to_customerHomePage)
            else -> { checkUser() }
            //navController.navigate(R.id.action_homePage_to_driverHomePageFragment)
        }
    }

    private fun checkUser() {
        firebaseAuth.signOut()
        navController.navigate(R.id.action_homePage_to_mainFragment2)
    }
}