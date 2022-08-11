package com.dormdash.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dormdash.android.R
import com.dormdash.android.databinding.FoodListBinding
import com.dormdash.android.database.FoodList
import com.dormdash.android.ui.FoodMenuFragment
import com.dormdash.android.ui.FoodMenuFragmentDirections
import com.dormdash.android.viewmodel.SharedCartViewModel

class FoodAdapter(
    private var c: FoodMenuFragment,
    var foodList: ArrayList<FoodList>,
    var menuId: String
    ):RecyclerView.Adapter<FoodAdapter.FoodListViewHolder>() {
    inner class  FoodListViewHolder(var v: FoodListBinding): RecyclerView.ViewHolder(v.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = DataBindingUtil.inflate<FoodListBinding>(
            inflater, R.layout.food_list,parent,false)
        return FoodListViewHolder(v)

    }

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        val newList = foodList[position]
        holder.v.isFood = foodList[position]
        holder.v.root.setOnClickListener {
            val foodDescription = newList.foodDescription
            val foodImg = newList.foodImg
            val foodName = newList.foodName
            val foodPrice = newList.foodPrice
            val foodCal = newList.foodCal

            holder.itemView.findNavController().navigate(FoodMenuFragmentDirections.actionFoodMenuFragmentToFoodConfirmationFragment
                (foodDescription, foodImg, foodName, foodPrice, menuId))
        }
    }



    override fun getItemCount(): Int {
        return  foodList.size
    }
}
