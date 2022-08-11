package com.dormdash.android.ui

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dormdash.android.R
import com.dormdash.android.adapter.RestaurantAdapter
import com.dormdash.android.databinding.FragmentCustomerHomePageBinding
import com.dormdash.android.database.Restaurants
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.firebase.database.*
import com.dormdash.android.database.Food
import com.dormdash.android.database.FoodItem
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomerHomePage : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private lateinit var mDataBase: DatabaseReference
    private lateinit var restaurantList:ArrayList<Restaurants>
    private lateinit var mAdapter:RestaurantAdapter
    private lateinit var FoodItemList: ArrayList<Food>
    private lateinit var cartList:ArrayList<FoodItem>
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var bottomNavigationView: BottomNavigationView
    private var fragmentCustomerHomePageBinding: FragmentCustomerHomePageBinding? = null
    private val viewModel : SharedCartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCustomerHomePageBinding.inflate(inflater, container, false)
        fragmentCustomerHomePageBinding = binding
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val search = view?.findViewById<SearchView>(R.id.search_bar).toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        restaurantList = ArrayList()
        FoodItemList = ArrayList()
        cartList = ArrayList()
        cartList = viewModel.getList() as ArrayList<FoodItem>
        mAdapter = RestaurantAdapter(this,restaurantList)
        searchView = view.findViewById(R.id.search_bar)
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)
        fragmentCustomerHomePageBinding!!.recyclerRestaurant.layoutManager = LinearLayoutManager(context)
        fragmentCustomerHomePageBinding!!.recyclerRestaurant.setHasFixedSize(true)
        fragmentCustomerHomePageBinding!!.recyclerRestaurant.adapter = mAdapter
        fragmentCustomerHomePageBinding!!.addressButton.setOnClickListener(this)
        restaurantData(null)
        updateButton()
        bottomClick()
        searchClick()
    }
    private fun updateButton() {
        FoodItemList = viewModel.getList() as ArrayList<Food>
        Log.e(TAG, "*******************UpdateButton in MENU*************: ${FoodItemList.size}")
        if (!FoodItemList.isEmpty() ){
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.size}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).description}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).img}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).name}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).price}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).quantity}")
            //fragmentCustomerHomePageBinding!!.viewCartButton.visibility = View.VISIBLE
        }
    }

    private fun restaurantData(search: CharSequence?) {
        restaurantList.clear()
        mDataBase = FirebaseDatabase.getInstance().getReference("Restaurants")
        mDataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    Log.i(TAG, "MyClass.getView() — in onDataChange")
                    for (restaurantSnapshot in snapshot.children){
                        val restaurant = restaurantSnapshot.getValue(Restaurants::class.java)
                        Log.i(TAG, "MyClass.getView() — in onDataChange")
                        Log.e(TAG, "error — in onDataChange")
                        if(search != null) {
                            if (restaurant!!.nameRestaurants?.contains(search.toString(), ignoreCase = true) == true)
                                restaurantList.add(restaurant)
                        }
                        else
                            restaurantList.add(restaurant!!)
                    }
                    fragmentCustomerHomePageBinding!!.recyclerRestaurant.adapter = mAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bottomClick(){
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome -> {
                    restaurantData(null)
                    view?.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_bar)?.visibility = View.INVISIBLE
                    view?.findViewById<Button>(R.id.address_button)?.visibility = View.VISIBLE
                }
                R.id.miSearch -> {
                    view?.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_bar)?.visibility = View.VISIBLE
                    view?.findViewById<Button>(R.id.address_button)?.visibility = View.INVISIBLE
                }
                R.id.miCart -> {
                    if (cartList.isEmpty()){
                        Toast.makeText(requireContext(), "Cart is Empty" , Toast.LENGTH_SHORT).show()
                    }
                    else
                        navController.navigate(R.id.action_customerHomePage_to_viewCartFragment)
                }
                R.id.miAccount -> navController.navigate(R.id.action_customerHomePage_to_customerAccountPage)
                R.id.miOrderStatus -> if (!viewModel.currentOrder){
                    Toast.makeText(requireContext(), "There is no order in progress." , Toast.LENGTH_SHORT).show()
                }
                else
                    navController.navigate(R.id.action_customerHomePage_to_orderStatusFragment)
            }
            true
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.view_cart_button -> bottomClick()//navController.navigate(R.id.action_customerHomePage_to_viewCartFragment)
            R.id.address_button -> navController.navigate(R.id.action_customerHomePage_to_customerAccountPage)
        }
    }

    private fun searchClick(){
        val search = view?.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_bar)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(query: String): Boolean {
                if (search != null) {
                    restaurantData(search.query)
                }
                fragmentCustomerHomePageBinding!!.recyclerRestaurant.adapter!!.notifyDataSetChanged()
                return true
            }
        })
    }
}