package com.dormdash.android.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.FragmentOrderStatusBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*


class OrderStatusFragment : Fragment() {
    private var customerOrderStatusBinding: FragmentOrderStatusBinding? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    lateinit var navController: NavController
    private lateinit var cartList:ArrayList<FoodItem>
    private val cartModel : SharedCartViewModel by activityViewModels()
    private lateinit var progress1 : View
    private lateinit var progress2 : View
    private lateinit var progress3 : View
    private lateinit var progress4 : View
    private lateinit var progress5 : View
    private lateinit var progress6 : View
    private lateinit var completeButton: Button
    private lateinit var workDataBase: DatabaseReference
    private lateinit var animation: Animation
    private lateinit var updateText : TextView
    private lateinit var driverPhone : String
    private lateinit var messageButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentOrderStatusBinding.inflate(inflater, container, false)
        customerOrderStatusBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.miOrderStatus
        navController = Navigation.findNavController(view)
        cartList = ArrayList()
        cartList = cartModel.getList() as ArrayList<FoodItem>
        progress1 = view.findViewById(R.id.progress1)
        progress2 = view.findViewById(R.id.progress2)
        progress3 = view.findViewById(R.id.progress3)
        progress4 = view.findViewById(R.id.progress4)
        progress5 = view.findViewById(R.id.progress5)
        progress6 = view.findViewById(R.id.progress6)
        progress1.alpha = 1.0f
        progress2.alpha = 0.3f
        progress3.alpha = 0.3f
        progress4.alpha = 0.3f
        progress5.alpha = 0.3f
        progress6.alpha = 0.3f
        completeButton = view.findViewById(R.id.confirm_pickup_button)
        completeButton.visibility = View.INVISIBLE
        messageButton = view.findViewById(R.id.message_button)
        messageButton.visibility = View.INVISIBLE
        updateText = view.findViewById(R.id.update_text)
        driverPhone = "1234567890"
        customerOrderStatusBinding!!.messageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$driverPhone")
            startActivity(intent)
        }

        animation = AlphaAnimation(1.0f, 0.3f)
        animation.duration = 2000
        animation.repeatCount = Int.MAX_VALUE
        progress1.startAnimation(animation)

        bottomClick()
        complete()
        checkDriver()
    }

    private fun bottomClick(){
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome -> {
                    navController.navigate(R.id.action_orderStatusFragment_to_customerHomePage)
                }
                R.id.miSearch -> {
                    navController.navigate(R.id.action_orderStatusFragment_to_customerHomePage)
                }
                R.id.miCart -> {
                    if (cartList.isEmpty()){
                        Toast.makeText(requireContext(), "Cart is Empty" , Toast.LENGTH_SHORT).show()
                    }
                    else
                        navController.navigate(R.id.action_orderStatusFragment_to_viewCartFragment)
                }
                R.id.miAccount -> {
                    navController.navigate(R.id.action_orderStatusFragment_to_customerAccountPage)
                }
            }
            true
        }
    }

    private fun checkDriver(){
        val path = "Restaurants/${cartModel.restaurantIDtemp}/Orders/${cartModel.orderIDtemp}/driverID"
        workDataBase = FirebaseDatabase.getInstance().getReference(path)
        workDataBase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val driverID = snapshot.value.toString()
                    checkProgress(driverID)
                }
                else{
                    Handler(Looper.getMainLooper()).postDelayed({
                        checkDriver()
                    }, 5000)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkProgress(driverID: String) {
                    var driverName = ""
                    workDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Assigned/${driverID}/driverFirstName")
                    workDataBase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists())
                                driverName = snapshot.value as String
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })

                    workDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Assigned/${driverID}/driverPhoneNumber")
                    workDataBase.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists())
                                driverPhone = snapshot.value as String
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })

                    workDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Assigned/${driverID}/status")
                    workDataBase.addValueEventListener(object : ValueEventListener {
                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                when (snapshot.value.toString().toInt()) {
                                    1 -> {
                                        progress1.alpha = 1.0f
                                        progress1.startAnimation(animation)
                                        updateText.text = "Your order has been sent! Waiting for restaurant to confirm."
                                    }
                                    2 -> {
                                        progress1.clearAnimation()
                                        progress1.alpha = 1.0f
                                        progress2.alpha = 1.0f
                                        progress2.startAnimation(animation)
                                        updateText.text = "Restaurant confirmed your order! Assigning Driver now..."
                                    }
                                    3 -> {
                                        progress2.clearAnimation()
                                        progress2.alpha = 1.0f
                                        progress3.alpha = 1.0f
                                        progress3.startAnimation(animation)
                                        updateText.text = "Driver found! $driverName is on the way to pick up your order. Use your messaging app if you need to contact them."
                                        messageButton.visibility = View.VISIBLE
                                    }
                                    4 -> {
                                        progress2.clearAnimation()
                                        progress3.clearAnimation()
                                        progress3.alpha = 1.0f
                                        progress4.alpha = 1.0f
                                        progress4.startAnimation(animation)
                                        updateText.text = "$driverName made it to the restaurant and is waiting for your order."
                                        messageButton.visibility = View.VISIBLE
                                    }
                                    5 -> {
                                        progress4.clearAnimation()
                                        progress4.alpha = 1.0f
                                        progress5.alpha = 1.0f
                                        progress5.startAnimation(animation)
                                        updateText.text = "$driverName is on the way to drop off your food."
                                        messageButton.visibility = View.VISIBLE
                                    }
                                    6 -> {
                                        progress5.clearAnimation()
                                        progress5.alpha = 1.0f
                                        progress6.alpha = 1.0f
                                        progress6.startAnimation(animation)
                                        updateText.text = "Your food has been delivered! Enjoy!"
                                        completeButton.visibility = View.VISIBLE
                                        messageButton.visibility = View.VISIBLE
                                        navController.navigate(R.id.customerHomePage)
                                        cartModel.currentOrder = false
                                        // need to go to home screen and delete/reset all view models
                                    }
                                }
                            }
                }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
    }

    private fun complete(){
        completeButton.setOnClickListener {
            cartModel.currentOrder = false
            navController.navigate(R.id.action_orderStatusFragment_to_customerHomePage)
        }
    }
}