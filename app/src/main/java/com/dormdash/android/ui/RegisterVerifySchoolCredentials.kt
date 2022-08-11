package com.dormdash.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentRegisterVerifySchoolCredentialsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.dormdash.android.database.Students
import com.google.firebase.auth.FirebaseUser

class RegisterVerifySchoolCredentials : Fragment(), View.OnClickListener {
    private var registerVerifySchoolCredentialsBinding: FragmentRegisterVerifySchoolCredentialsBinding? = null
    lateinit var navController: NavController
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var regFirstName: EditText
    lateinit var regLastName: EditText
    lateinit var regPhoneNumber: EditText
    lateinit var regSchoolId: EditText
    lateinit var regEmail: EditText
    lateinit var regPassword: EditText
    lateinit var registerVerify: Button
    lateinit var registerBtn: Button
    private var nccEmail ="@noctrl.edu"

    //private variables needed to work with RealtimeDatabase
    private lateinit var database: FirebaseDatabase
    private lateinit var referance: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val binding = FragmentRegisterVerifySchoolCredentialsBinding.inflate(inflater, container, false)
        registerVerifySchoolCredentialsBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerVerifySchoolCredentialsBinding
        firebaseAuth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)

        regFirstName = registerVerifySchoolCredentialsBinding?.registerFirstNameText!!
        regLastName = registerVerifySchoolCredentialsBinding?.registerLastNameEditText!!
        regPhoneNumber = registerVerifySchoolCredentialsBinding?.registerPhoneNumEditText!!
        regSchoolId = registerVerifySchoolCredentialsBinding?.registerIDEditText3!!
        regEmail = registerVerifySchoolCredentialsBinding?.registerEmailEditText!!
        regPassword = registerVerifySchoolCredentialsBinding?.registerPaswrdEditText!!

        registerVerify = registerVerifySchoolCredentialsBinding?.registerVerifyButton!!
        registerBtn = registerVerifySchoolCredentialsBinding?.registerRegisterButton!!
        registerVerify.visibility = View.INVISIBLE
        registerVerify.setOnClickListener { verifyMe() }
        registerBtn.setOnClickListener { validateData() }

        //connecting to RealtimeDatabase
        database = FirebaseDatabase.getInstance();
        referance = database.getReference("Users")  //creating Users under RealtimeDatabase

        registerVerifySchoolCredentialsBinding?.registerVerifyCancelButton?.setOnClickListener(this)
    }


    private fun validateData(
    ) {
        firebaseAuth.currentUser?.reload()
        val first = regFirstName.text.toString().trim()
        val last = regLastName.text.toString().trim()
        val phone = regPhoneNumber.text.toString().trim()
        val id = regSchoolId.text.toString().trim()
        val email = regEmail.text.toString().trim()
        val password = regPassword.text.toString().trim()

        if (first.isEmpty()) { regFirstName.error = "Enter First Name" }
        if (last.isEmpty()) { regLastName.error = "Enter Last Name" }
        if (phone.isEmpty()) { regPhoneNumber.error = "Enter Phone Number" }
        if (id.isEmpty()) { regSchoolId.error = "Enter School ID" }
        if (email.isEmpty()) { regEmail.error = "Enter School Email" }
        if (password.isEmpty()) { regPassword.error = "Enter Password" }

        if (first.isNotEmpty() && last.isNotEmpty() && phone.isNotEmpty() && id.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
            if (phone.isNotEmpty() && phone.length != 10 ) { regPhoneNumber.error = "Enter valid Phone Number" }
            if (id.isNotEmpty() && id.length != 7 ) { regSchoolId.error = "Enter valid ID" }
            if (email.isNotEmpty() && !email.contains(nccEmail) ) { regEmail.error = "Enter valid NCC Email" }
            if (password.isNotEmpty() && password.length <= 5 ) { regPassword.error = "Password must have 6 Characters or more" }
            else if (email.isNotEmpty() && email.contains(nccEmail)) {
                firebaseSignup(email,password)
            }
        }
        else {
            Toast.makeText(context, "Must Enter All Lines", Toast.LENGTH_SHORT).show()
        }
    }


    private fun firebaseSignup(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
        .addOnSuccessListener {
            val fireBaseUser = firebaseAuth.currentUser
            val firebaseEmail = fireBaseUser!!.email
            firebaseAuth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                Toast.makeText(context, "Sent Verification Email to $firebaseEmail", Toast.LENGTH_SHORT).show()
            }
            firebaseAuth.currentUser!!.sendEmailVerification().addOnCompleteListener{
                emailSent()
            }
            firebaseAuth.currentUser!!.sendEmailVerification().addOnFailureListener { e->
                Toast.makeText(context, "Registered Failed due to ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendDataToRealtimeDB(user: FirebaseUser?) {
        /*
        This class creates instance of Student User as Entity and
        sends it to Firebase Realtime Database
         */

            val uid = user!!.uid
            val fName = regFirstName.text.toString().trim()
            val lName = regLastName.text.toString().trim()
            val pNumber = regPhoneNumber.text.toString().trim()
            val sID = regSchoolId.text.toString().trim()
            val email = regEmail.text.toString().trim()
            val pass = regPassword.text.toString().trim()
            //creating Student variable from Data class
            val studentUser = Students(sID, fName, lName, pNumber, email, pass, uid )

            //setting Student ID as Unique Identifier and adding Student User "Entity"
            referance.child(firebaseAuth.currentUser!!.uid).setValue(studentUser)
    }

    private fun emailSent() {
        val user = firebaseAuth.currentUser?.reload()
        registerBtn.visibility = View.INVISIBLE
        registerVerify.visibility = VISIBLE
        sendDataToRealtimeDB(firebaseAuth.currentUser)
    }


    private fun verifyMe() {
        firebaseAuth.currentUser?.reload()
        if (firebaseAuth.currentUser!!.isEmailVerified){
                navController.navigate(R.id.action_registerVerifySchoolCredentials_to_homePage)
        } else{
            Toast.makeText(context, "Verification Email Sent", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.register_verify_cancel_button -> navController.navigate(R.id.action_registerVerifySchoolCredentials_to_registerFindSchool)
        }
    }
}