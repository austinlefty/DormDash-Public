package com.dormdash.android.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.CartListBinding
import com.dormdash.android.databinding.FragmentFoodOrderConfirmationBinding
import com.dormdash.android.ui.FoodOrderConfirmationFragment
import com.dormdash.android.viewmodel.SharedCartViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

class PaymentAdapter(
    private var c: FoodOrderConfirmationFragment,
    var cartList: ArrayList<FoodItem>,
    var fragmentFoodOrderConfirmationBinding: FragmentFoodOrderConfirmationBinding,
    var navController: NavController,
    var viewModel: SharedCartViewModel
): RecyclerView.Adapter<PaymentAdapter.CartViewHolder>() {
    inner class  CartViewHolder(var v: CartListBinding): RecyclerView.ViewHolder(v.root){}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = DataBindingUtil.inflate<CartListBinding>(
            inflater, R.layout.cart_list,parent,false)

        return CartViewHolder(v)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val newList = cartList[position]
        holder.v.isCart = cartList[position]
        holder.v.cartDeleteButton.setOnClickListener { deleteItem(position, holder )}
        holder.v.root.setOnClickListener {
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteItem(position: Int, holder: CartViewHolder) {
        var sub = fragmentFoodOrderConfirmationBinding.subtotalNumberText.text.substring(1, fragmentFoodOrderConfirmationBinding.subtotalNumberText.text.length).toDouble()
        sub -= cartList[position].price.substring(1, cartList[position].price.length).toDouble()
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val subPriceRounded = df.format(sub.toDouble())
        fragmentFoodOrderConfirmationBinding.subtotalNumberText.text = subPriceRounded.toString()
        cartList.removeAt(position)
        holder.bindingAdapter!!.notifyDataSetChanged()
        updateFees(subPriceRounded)
        checkIfEmpty()
    }
    private fun checkIfEmpty() {
        if (cartList.size == 0){
            fragmentFoodOrderConfirmationBinding.totalNumberText.text = "0.00"
            fragmentFoodOrderConfirmationBinding.subtotalNumberText.text = "0.00"
            fragmentFoodOrderConfirmationBinding.taxNumberText.text = "0.00"
            fragmentFoodOrderConfirmationBinding.serviceFeeNumberText.text = "0.00"
            fragmentFoodOrderConfirmationBinding.totalNumberText.text = "0.00"
            viewModel.restaurantID = null
            navController.navigate(R.id.customerHomePage)
        }
    }

    private fun updateFees(subPriceRounded: String) {
        val grandTotal = getDeliveryFee(subPriceRounded.toDouble()) + getServiceFee(subPriceRounded.toDouble()) + getTax(subPriceRounded.toDouble()) + subPriceRounded.toDouble()
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(grandTotal)
        fragmentFoodOrderConfirmationBinding.totalNumberText.text = rounded.toString()
    }

    private fun getTax(subPrice: Double): Double {
        val tax = 0.0625
        val totalTax = subPrice.times(tax)
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(totalTax)
        fragmentFoodOrderConfirmationBinding.taxNumberText.text = rounded.toString()
        return rounded.toDouble()

    }

    private fun getServiceFee(subPrice: Double): Double {
        val fee = 0.15
        val totalFee = subPrice.times(fee)
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(totalFee)
        fragmentFoodOrderConfirmationBinding.serviceFeeNumberText.text = rounded.toString()
        return rounded.toDouble()
    }

    private fun getDeliveryFee(subPrice: Double): Double {
        val fee = 0.05
        val totalFee = subPrice.times(fee)
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(totalFee)
        fragmentFoodOrderConfirmationBinding.deliveryFeeNumberText.text = rounded.toString()
        return rounded.toDouble()
    }

    override fun getItemCount(): Int {
        return  cartList.size
    }


}
