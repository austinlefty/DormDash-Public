package com.dormdash.android.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dormdash.android.R
import com.dormdash.android.adapter.CartAdapter
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.FragmentViewCartBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.firebase.auth.FirebaseAuth
import java.math.RoundingMode
import java.text.DecimalFormat


class ViewCartFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cartList:ArrayList<FoodItem>
    private lateinit var mAdapter: CartAdapter
    private var fragmentViewCartBinding: FragmentViewCartBinding? = null
    private val viewModel : SharedCartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentViewCartBinding.inflate(inflater, container, false)
        fragmentViewCartBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        cartList = ArrayList()
        cartList = viewModel.getList() as ArrayList<FoodItem>
        var subPrice = 0.00
        viewModel.getList().forEach { subPrice += it.price.substring(1, it.price.length).toDouble() }
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val subPriceRounded = df.format(subPrice)
        Log.i(ContentValues.TAG, "@@@@@@@@@@@@@@@@@@SubTotal@@@@@@@@@@@@@@@@@@ : $subPriceRounded")
        fragmentViewCartBinding!!.subTotalTextView.text = subPriceRounded.toString()
        mAdapter = CartAdapter(this,viewModel.getList() as ArrayList<FoodItem>,  fragmentViewCartBinding!!, viewModel, navController)
        fragmentViewCartBinding!!.recyclerCart.layoutManager = LinearLayoutManager(context)
        fragmentViewCartBinding!!.recyclerCart.adapter = mAdapter
        fragmentViewCartBinding!!.viewOrderButton.setOnClickListener(this)

//        Log.i(ContentValues.TAG, "name : ${viewModel.getName(0)}")
//        Log.i(ContentValues.TAG, "image : ${viewModel.getImg(0)}")
//        Log.i(ContentValues.TAG, "foodPrice ${viewModel.getPrice(0)}")
//        Log.i(ContentValues.TAG, "quantity : ${viewModel.getQuantity(0)}")
//        Log.i(ContentValues.TAG, "description ${viewModel.getDescription(0)}")


    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.view_order_button -> navController.navigate(R.id.action_viewCartFragment_to_foodOrderConfirmationFragment)
        }
    }


}