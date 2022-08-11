package com.dormdash.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.Navigation
import android.widget.AdapterView
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentRegisterFindSchoolBinding


class RegisterFindSchool : Fragment(), View.OnClickListener, AdapterView.OnItemClickListener {
    private var fragmentRegisterFindSchoolBinding: FragmentRegisterFindSchoolBinding? = null
    private lateinit var navController: NavController
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val binding = FragmentRegisterFindSchoolBinding.inflate(inflater, container, false)
        fragmentRegisterFindSchoolBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        fragmentRegisterFindSchoolBinding?.spinnerView?.adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.schools, android.R.layout.simple_spinner_item)
        (fragmentRegisterFindSchoolBinding?.spinnerView?.adapter as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Button>(R.id.register_find_cancel_button).setOnClickListener(this)
        nextButton = view.findViewById(R.id.register_find_next_button)
        searchSpinner(navController, fragmentRegisterFindSchoolBinding!!.registerFindNextButton)
    }

    private fun searchSpinner(navController: NavController, registerFindNextButton: Button) {
        registerFindNextButton.isEnabled = false
        fragmentRegisterFindSchoolBinding?.spinnerView?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
            View.OnClickListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                postition: Int,
                id: Long
            ) {
                val selectedItem = parent!!.getItemAtPosition(postition)
                if (postition == 21) {
                    //view!!.findViewById<Button>(R.id.register_find_next_button).setOnClickListener(this)
                    fragmentRegisterFindSchoolBinding?.registerFindNextButton?.setOnClickListener(this)
                    fragmentRegisterFindSchoolBinding?.registerFindNextButton?.isEnabled =  true
                }
                else {
                    Toast.makeText(context, "$selectedItem is not yet available for DormDash", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                fragmentRegisterFindSchoolBinding?.registerFindNextButton?.isEnabled = false
            }

            override fun onClick(v: View?) {
                when (v!!.id) {
                    R.id.register_find_next_button -> navController.navigate(R.id.action_registerFindSchool_to_registerVerifySchoolCredentials)
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.register_find_cancel_button -> navController.navigate(R.id.action_registerFindSchool_to_mainFragment2)
        }
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, positon: Int, id: Long) {
        if (requireView().findViewById<Button>(R.id.register_find_next_button).isEnabled) {
            if (view != null) {
                when (view.id) {
                    R.id.register_find_next_button -> navController.navigate(R.id.action_registerFindSchool_to_registerVerifySchoolCredentials)
                }
            }
        }
    }
}