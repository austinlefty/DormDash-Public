package com.dormdash.android.ui

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.database.Students
import com.dormdash.android.databinding.FragmentCustomerHomePageBindingImpl
import com.dormdash.android.databinding.FragmentLoginBinding
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(), View.OnClickListener {
    private lateinit var database: DatabaseReference
    private var fragmentLoginBinding: FragmentLoginBinding? = null
    lateinit var navController: NavController
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var schoolEmailEV: EditText
    lateinit var passwordEV: EditText
    lateinit var loginButton: Button
    private val viewModel : CurrentUserSharedViewModel by activityViewModels()
    // need to put Ncc email to database instead of hardcoding it in
    val nccEmail ="@noctrl.edu"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        fragmentLoginBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        schoolEmailEV = view.findViewById(R.id.login_email)
        passwordEV = view.findViewById(R.id.login_password)
        view.findViewById<Button>(R.id.login_cancel_button).setOnClickListener(this)
        loginButton = view.findViewById(R.id.login_next_button)
        loginButton.setOnClickListener{ authenticateMe() }
        //checkUser()
    }

//    private fun checkUser() {
//        val firebaseUser = firebaseAuth.currentUser
//        if (firebaseUser != null){
//            if (firebaseUser.isEmailVerified){
//                database = Firebase.database.reference
//                val user = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
//                user.get().addOnSuccessListener {
//                    if (it.exists()){
//                        viewModel.currentUser = Students()
//                        viewModel.currentUser!!.firstName = it.child("firstName").value.toString()
//                        viewModel.currentUser!!.lastName  = it.child("lastName").value.toString()
//                        viewModel.currentUser!!.phoneNum  = it.child("phoneNum").value.toString()
//                        viewModel.currentUser!!.studentID = it.child("studentID").value.toString()
//                        viewModel.currentUser!!.email = it.child("email").value.toString()
//                        Log.i(TAG, "*******************Student Info firstname: *************: ${viewModel.currentUser!!.firstName}")
//                        Log.i(TAG, "*******************Student Info lastName: *************: ${viewModel.currentUser!!.lastName}")
//                        Log.i(TAG, "*******************Student Info phoneNum: *************: ${viewModel.currentUser!!.phoneNum}")
//                        Log.i(TAG, "*******************Student Info studentID: *************: ${viewModel.currentUser!!.studentID}")
//                        Log.i(TAG, "*******************Student Info firstname: *************: ${viewModel.currentUser!!.lastName }")
//                        navController.navigate(R.id.action_loginFragment_to_homePage)
//                    }
//                }
//            }else{
//                Toast.makeText(context, "Email Not Verified", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun authenticateMe() {
        val email = schoolEmailEV.text.toString().trim()
        val password = passwordEV.text.toString().trim()

        if (email.isEmpty()) { schoolEmailEV.error = "Enter School Email" }
        if (password.isEmpty()) { passwordEV.error = "Enter Password" }
        if (email.isNotEmpty() && password.isNotEmpty()){
            if (email.isNotEmpty() && !email.contains(nccEmail) ) { schoolEmailEV.error = "Enter valid NCC Email" }
            if (password.isNotEmpty() && password.length <= 5 ) { passwordEV.error = "Password must have 6 Characters or more" }
            else {
                login(email,password)
            }
        }
    }

    private fun login(email: String, password: String) {
        val firebaseUser = firebaseAuth.currentUser
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified){
                        Log.d(TAG, "signInWithEmail:success")
                        database = Firebase.database.reference
                        val user = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
                        user.get().addOnSuccessListener {
                            if (it.exists()){
                                viewModel.currentUser = Students()
                                viewModel.currentUser!!.firstName = it.child("firstName").value.toString()
                                viewModel.currentUser!!.lastName  = it.child("lastName").value.toString()
                                viewModel.currentUser!!.phoneNum  = it.child("phoneNum").value.toString()
                                viewModel.currentUser!!.studentID = it.child("studentID").value.toString()
                                viewModel.currentUser!!.email = it.child("email").value.toString()
                                viewModel.currentUser!!.uid = it.child("uid").value.toString()
                                Log.e(TAG, "|||*******************Student Info firstname: *************|||: ${viewModel.currentUser!!.firstName}")
                                Log.e(TAG, "*******************Student Info lastName: *************: ${viewModel.currentUser!!.lastName}")
                                Log.e(TAG, "*******************Student Info phoneNum: *************: ${viewModel.currentUser!!.phoneNum}")
                                Log.e(TAG, "*******************Student Info studentID: *************: ${viewModel.currentUser!!.studentID}")
                                Log.e(TAG, "*******************Student Info email: *************: ${viewModel.currentUser!!.email }")
                                Log.e(TAG, "|||*******************Student Info uid: *************|||: ${viewModel.currentUser!!.uid }")

                            }
                        }
                        navController.navigate(R.id.action_loginFragment_to_homePage)
                    }else{
                        Toast.makeText(context, "Must Verify Your Email", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "signInWithEmail:failure")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Login Failed due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

//    private fun updateUI(user: FirebaseUser?) {
//        database = Firebase.database.reference
//        Log.e(TAG, "*******************Student Info ID: *************: ${user!!.uid}")
//        val userInfo = FirebaseDatabase.getInstance().getReference("Users").child(user.uid.toString())
//        userInfo.get().addOnSuccessListener  {
//            viewModel.currentUser = Students()
//            viewModel.currentUser!!.firstName = it.child("firstName").value.toString()
//            viewModel.currentUser!!.lastName  = it.child("lastName").value.toString()
//            viewModel.currentUser!!.phoneNum  = it.child("phoneNum").value.toString()
//            viewModel.currentUser!!.studentID = it.child("studentID").value.toString()
//            viewModel.currentUser!!.uid = it.child("uid").value.toString()
//            viewModel.currentUser!!.email = it.child("email").value.toString()
//            Log.e(TAG, "*******************Student Info firstname: *************: ${viewModel.currentUser!!.firstName}")
//            Log.e(TAG, "*******************Student Info lastName: *************: ${viewModel.currentUser!!.lastName}")
//            Log.e(TAG, "*******************Student Info phoneNum: *************: ${viewModel.currentUser!!.phoneNum}")
//            Log.e(TAG, "*******************Student Info studentID: *************: ${viewModel.currentUser!!.studentID}")
//            Log.e(TAG, "*******************Student Info UID: *************: ${viewModel.currentUser!!.uid}")
//            Log.e(TAG, "*******************Student Info firstname: *************: ${viewModel.currentUser!!.lastName }")
//            navController.navigate(R.id.action_loginFragment_to_homePage)
//        }
//    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.login_cancel_button -> navController.navigate(R.id.action_loginFragment_to_mainFragment2)
        }
    }

}
