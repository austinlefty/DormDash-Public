package com.dormdash.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dormdash.android.R
import com.dormdash.android.databinding.RestaurantListBinding
import com.dormdash.android.database.Restaurants
import com.dormdash.android.ui.CustomerHomePage
import com.dormdash.android.ui.CustomerHomePageDirections

class RestaurantAdapter(
    private var c: CustomerHomePage, private var restaurantList:ArrayList<Restaurants>
):RecyclerView.Adapter<RestaurantAdapter.RestaurantsViewHolder>() {
    inner class  RestaurantsViewHolder(var v:RestaurantListBinding): RecyclerView.ViewHolder(v.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantsViewHolder {
                val inflater = LayoutInflater.from(parent.context)
        val v = DataBindingUtil.inflate<RestaurantListBinding>(
            inflater, R.layout.restaurant_list,parent,false)
        return RestaurantsViewHolder(v)

    }

    override fun onBindViewHolder(holder: RestaurantsViewHolder, position: Int) {
        val newList = restaurantList[position]
        holder.v.isRestaurant = restaurantList[position]
        holder.v.root.setOnClickListener {
            val img = newList.img
            val name = newList.nameRestaurants
            val address = newList.address
            val deliveryFee = newList.deliveryFee
            val eta = newList.eta
            val menuId = newList.menuID
            holder.itemView.findNavController().navigate(CustomerHomePageDirections.actionCustomerHomePageToFoodMenuFragment(name, deliveryFee, img, address, eta, menuId))
        }
    }

    override fun getItemCount(): Int {
        return  restaurantList.size
    }
}
