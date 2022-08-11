package com.dormdash.android.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dormdash.android.R
import com.dormdash.android.adapter.FoodAdapter
import com.dormdash.android.database.FoodItem
import com.dormdash.android.database.FoodList
import com.dormdash.android.databinding.FragmentFoodMenuBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.firebase.database.*


class FoodMenuFragment :Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private lateinit var mDataBase: DatabaseReference
    private lateinit var foodList:ArrayList<FoodList>
    private lateinit var menuId:String
    private lateinit var mAdapter: FoodAdapter
    private lateinit var FoodItemList: ArrayList<FoodItem>
    private val viewModel : SharedCartViewModel by activityViewModels()

    private var fragmentFoodMenuBinding: FragmentFoodMenuBinding? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFoodMenuBinding.inflate(inflater, container, false)
        fragmentFoodMenuBinding = binding
        val args = FoodMenuFragmentArgs.fromBundle(requireArguments())
        if (args.img == "null"){
            fragmentFoodMenuBinding!!.restaurantImgMenu.setImageResource(R.drawable.appicon)
            Log.e(ContentValues.TAG, "img — ${args.img}")
        }else{
                Glide.with(this)
                    .load(args.img)
                    .centerCrop()
                    .into(fragmentFoodMenuBinding!!.restaurantImgMenu)
        }
        fragmentFoodMenuBinding!!.deliveryFeeMenu.text = "$" + args.fee + " Delivery Fee"
        fragmentFoodMenuBinding!!.addressMenu.text = args.address
        fragmentFoodMenuBinding!!.deliveryEtaMenu.text = args.eta + " min"
        fragmentFoodMenuBinding!!.collapsingToolBar.title = args.name
        FoodItemList = ArrayList()
        menuId = args.menuId.toString()
        updateButton()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        foodList = ArrayList()
        mAdapter = FoodAdapter(this,foodList, menuId)
        fragmentFoodMenuBinding!!.viewCartButton.setOnClickListener(this)
        fragmentFoodMenuBinding!!.recyclerFoodView.layoutManager = LinearLayoutManager(context)
        fragmentFoodMenuBinding!!.recyclerFoodView.setHasFixedSize(false)
        fragmentFoodMenuBinding!!.recyclerFoodView.adapter = mAdapter
        restaurantData()
    }


    private fun updateButton() {
        FoodItemList = viewModel.getList() as ArrayList<FoodItem>
        Log.e(TAG, "*******************UpdateButton in MENU*************: ${FoodItemList.size}")
        if (!FoodItemList.isEmpty() ){
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.size}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).description}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).img}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).name}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).price}")
//            Log.e(TAG, "*******************ListCount in MENU*************: ${FoodItemList.get(1).quantity}")
            fragmentFoodMenuBinding!!.viewCartButton.visibility = View.VISIBLE

        }
    }


    private fun restaurantData() {
        val restaurant = "Restaurants"
        val path = "$restaurant/$menuId/menu"
        mDataBase = FirebaseDatabase.getInstance().getReference(path)
        mDataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    Log.i(TAG, "MyClass.getView() — in onDataChange")
                    for (foodSnapshot in snapshot.children){
                        val food = foodSnapshot.getValue(FoodList::class.java)
                        Log.i(TAG, "MyClass.getView() — in onDataChange")
                        Log.e(TAG, "error — in onDataChange")
                        foodList.add(food!!)
                    }
                    fragmentFoodMenuBinding!!.recyclerFoodView.adapter = mAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.view_cart_button -> navController.navigate(R.id.action_foodMenuFragment_to_viewCartFragment)
        }
    }
}

