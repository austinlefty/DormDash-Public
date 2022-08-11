package com.dormdash.android.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.ui.ViewCartFragment
import com.dormdash.android.databinding.CartListBinding
import com.dormdash.android.databinding.FragmentViewCartBinding
import com.dormdash.android.viewmodel.SharedCartViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

class CartAdapter(
    private var c: ViewCartFragment,
    var cartList: ArrayList<FoodItem>,
    var fragmentViewCartBinding: FragmentViewCartBinding,
    var viewModel: SharedCartViewModel,
    var navController: NavController
):RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
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
        var sub = fragmentViewCartBinding.subTotalTextView.text.substring(1, fragmentViewCartBinding.subTotalTextView.text.length).toDouble()
        sub -= cartList[position].price.substring(1, cartList[position].price.length).toDouble()
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val subPriceRounded = df.format(sub.toDouble())
        fragmentViewCartBinding.subTotalTextView.text = subPriceRounded.toString()
        cartList.removeAt(position)
        holder.bindingAdapter!!.notifyDataSetChanged()
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        if (cartList.size == 0){
            viewModel.restaurantID = null
            navController.navigate(R.id.customerHomePage)
        }
    }

    override fun getItemCount(): Int {
        return  cartList.size
    }
}
