package com.dormdash.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class MainFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private var fragmentMainBinding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        fragmentMainBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        fragmentMainBinding!!.mainLoginButton.setOnClickListener(this)
        fragmentMainBinding!!.mainRegisterButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.main_login_button -> checkAccess()
            R.id.main_register_button -> navController.navigate(R.id.action_mainFragment2_to_registerFindSchool)
        }
    }

    private fun checkAccess() {
        Dexter.withActivity(this.activity)
        .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        .withListener(object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                navController.navigate(R.id.action_mainFragment2_to_loginFragment)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                Toast.makeText(context, "Accept GPS Permission", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest?,
                token: PermissionToken?
            ) {
                TODO("Not yet implemented")
            }

        }).check()
    }
}