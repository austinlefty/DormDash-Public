package com.dormdash.android.ui

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.MainActivity
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.FragmentRegisterVerifySchoolCredentialsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.dormdash.android.database.Students
import com.dormdash.android.databinding.FragmentCustomerAccountPageBinding
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser

class CustomerAccountPage : Fragment(), View.OnClickListener {
    private var customerAccountPageBinding: FragmentCustomerAccountPageBinding? = null
    lateinit var navController: NavController
    private lateinit var mDataBase: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var editFirstName: EditText
    private lateinit var editLastName: EditText
    private lateinit var editPhoneNumber: EditText
    private lateinit var editSchoolId: EditText
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var saveButton: Button
    private lateinit var cartList:ArrayList<FoodItem>
    private val cartModel : SharedCartViewModel by activityViewModels()
    private val viewModel : CurrentUserSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentCustomerAccountPageBinding.inflate(inflater, container, false)
        customerAccountPageBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        cartList = ArrayList()
        cartList = cartModel.getList() as ArrayList<FoodItem>
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.miAccount

        editFirstName = customerAccountPageBinding?.firstNameText!!
        editLastName = customerAccountPageBinding?.lastNameText!!
        editPhoneNumber = customerAccountPageBinding?.phoneNumText!!
        editSchoolId = customerAccountPageBinding?.idText!!

        saveButton = view.findViewById(R.id.save_button)
        saveButton.setOnClickListener{ save() }

        fillText()
        bottomClick()
    }

    private fun bottomClick(){
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome -> {
                    navController.navigate(R.id.action_customerAccountPage_to_customerHomePage)
                }
                R.id.miSearch -> {
                    navController.navigate(R.id.action_customerAccountPage_to_customerHomePage)
                }
                R.id.miCart -> {
                    if (cartList.isEmpty()){
                        Toast.makeText(requireContext(), "Cart is Empty" , Toast.LENGTH_SHORT).show()
                    }
                    else
                        navController.navigate(R.id.action_customerAccountPage_to_viewCartFragment)
                }
                R.id.miOrderStatus -> if (!cartModel.currentOrder){
                    Toast.makeText(requireContext(), "There is no order in progress." , Toast.LENGTH_SHORT).show()
                }
                else
                    navController.navigate(R.id.action_customerAccountPage_to_orderStatusFragment)
            }
            true
        }
    }


    private fun fillText(){
        val firebaseUser = firebaseAuth.currentUser
        val user = firebaseUser?.let { FirebaseDatabase.getInstance().getReference("Users").child(it.uid) }
        user?.get()?.addOnSuccessListener {
            if (it.exists()){
                editFirstName.setText(viewModel.currentUser!!.firstName)
                editLastName.setText(viewModel.currentUser!!.lastName)
                editPhoneNumber.setText(viewModel.currentUser!!.phoneNum)
                editSchoolId.setText(viewModel.currentUser!!.studentID)
            }
        }
    }

    private fun save(){
        val firebaseUser = firebaseAuth.currentUser
        val user = firebaseUser?.let { FirebaseDatabase.getInstance().getReference("Users/${viewModel.currentUser?.uid.toString()}")}
        mDataBase = FirebaseDatabase.getInstance().getReference("Users")
        val children = mapOf(
            "firstName" to editFirstName.text.toString(),
            "lastName" to editLastName.text.toString(),
            "phoneNum" to editPhoneNumber.text.toString(),
            "studentID" to editSchoolId.text.toString()
        )
        if(editFirstName.text.toString().isNullOrEmpty() || editLastName.text.toString().isNullOrEmpty() ||
            editPhoneNumber.text.toString().isNullOrEmpty() || editSchoolId.text.toString().isNullOrEmpty()){
            Toast.makeText(context, "Changes not saved. Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
        else {
            mDataBase.child(viewModel.currentUser!!.uid.toString()).updateChildren(children)
                .addOnSuccessListener {
                    Toast.makeText(context, "Changes successfully saved!", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.currentUser!!.firstName = editFirstName.text.toString()
                    viewModel.currentUser!!.lastName = editLastName.text.toString()
                    viewModel.currentUser!!.phoneNum = editPhoneNumber.text.toString()
                    viewModel.currentUser!!.studentID = editSchoolId.text.toString()
                }.addOnFailureListener {
                Toast.makeText(context, "Changes to Save Failed", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}