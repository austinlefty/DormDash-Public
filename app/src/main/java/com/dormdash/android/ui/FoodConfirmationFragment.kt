package com.dormdash.android.ui

import android.content.ContentValues
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
import com.bumptech.glide.Glide
import com.dormdash.android.R
import com.dormdash.android.database.Cart
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.FragmentFoodConfirmationBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import java.math.RoundingMode
import java.text.DecimalFormat


class FoodConfirmationFragment :Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private lateinit var foodItem:ArrayList<Cart>
    private var fragmentFoodConfirmationBinding: FragmentFoodConfirmationBinding? = null
    private var foodPrice= ""
    var menuId: String? = null
    private lateinit var cartList: ArrayList<Cart>
    private var args: FoodConfirmationFragmentArgs? = null
    private val viewModel : SharedCartViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFoodConfirmationBinding.inflate(inflater, container, false)
        fragmentFoodConfirmationBinding = binding
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val args = FoodConfirmationFragmentArgs.fromBundle(requireArguments())
        if (args.foodImg == "null"){ fragmentFoodConfirmationBinding!!.foodImg.setImageResource(R.drawable.appicon) }
        else{
            Glide.with(this)
                .load(args.foodImg)
                .centerCrop()
                .into(fragmentFoodConfirmationBinding!!.foodImg)
        }
        menuId = args.menuID
        fragmentFoodConfirmationBinding!!.foodInfo.text = args.foodDescription
        fragmentFoodConfirmationBinding!!.foodName.text = args.foodName
        foodPrice = args.foodPrice.toString()
        fragmentFoodConfirmationBinding!!.foodPrice.text = foodPrice
        fragmentFoodConfirmationBinding!!.decrementButton.setOnClickListener { decreaseQuantity() }
        fragmentFoodConfirmationBinding!!.incrementButton.setOnClickListener { increaseQuantity() }
        fragmentFoodConfirmationBinding!!.addToCartButton.setOnClickListener { checkIfValid() }
        updateCartPrice()
    }

    private fun checkIfValid() {
        if (viewModel.getCount() == 0 && viewModel.restaurantID == null){
            Log.e(ContentValues.TAG, "*******************Valid() 1:*************: ${viewModel.getCount()}")
            valid()
        }
        else if (viewModel.getCount() > 0 && viewModel.restaurantID != null) {
            if (checkRestaurantID()) {
                Log.e(ContentValues.TAG, "*******************Valid() 2:*************: ${viewModel.getCount()}")
                valid()
            }
            else notValid()
        }
    }

    private fun notValid() {
        Toast.makeText(context, "You may only order food from one restaurant at a time", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.customerHomePage)
    }

    private fun valid() {
        val amount_text = fragmentFoodConfirmationBinding!!.foodPrice.text.toString()
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        //val amount_dec = df.format(amount_text.toDouble())

        Log.e(
            ContentValues.TAG,
            "*******************ListCount*************: ${viewModel.getCount()}"
        )
        viewModel.addItem(
            FoodItem(
                fragmentFoodConfirmationBinding!!.foodName.text.toString(),
                fragmentFoodConfirmationBinding!!.foodImg.toString(),
                fragmentFoodConfirmationBinding!!.foodPrice.text.toString(),
                fragmentFoodConfirmationBinding!!.quantityTextView.text.toString(),
                fragmentFoodConfirmationBinding!!.foodInfo.text.toString(),
                fragmentFoodConfirmationBinding!!.foodModifyText.text.toString()
            )
        )
        Log.e(
            ContentValues.TAG,
            "*******************ListCount*************: ${viewModel.getCount()}"
        )
        //name, deliveryFee, img, address, eta, menuId
        viewModel.restaurantID = menuId
        navController.popBackStack()
    }

    private fun checkRestaurantID() : Boolean {
        return menuId!! == viewModel.restaurantID
    }

    private fun increaseQuantity() {
        val quantity = fragmentFoodConfirmationBinding!!.quantityTextView.text.toString()
        var intQuantity = quantity.toInt()
        if(intQuantity < 20) {
            intQuantity++
            fragmentFoodConfirmationBinding!!.quantityTextView.text = intQuantity.toString()
            updateCartPrice()
        }
    }

    private fun decreaseQuantity() {
        val quantity = fragmentFoodConfirmationBinding!!.quantityTextView.text.toString()
        var intQuantity = quantity.toInt()
        if(intQuantity > 1) {
            intQuantity--
            fragmentFoodConfirmationBinding!!.quantityTextView.text = intQuantity.toString()
            updateCartPrice()
        }
    }

    private fun updateCartPrice() {
        val quantity = fragmentFoodConfirmationBinding!!.quantityTextView.text.toString()
        val intQuantity = quantity.toInt()
        val itemTotal = intQuantity.times(foodPrice.toDouble())
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val itemTotalRound = df.format(itemTotal)
        fragmentFoodConfirmationBinding!!.foodPrice.text = itemTotalRound.toString()
        //Log.i(ContentValues.TAG, "error — in onDataChange")
    }


    override fun onClick(v: View?) {
    }
}

