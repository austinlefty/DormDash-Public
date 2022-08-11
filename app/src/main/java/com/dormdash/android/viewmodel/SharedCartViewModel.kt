package com.dormdash.android.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.dormdash.android.database.FoodItem
import com.dormdash.android.database.Order

class SharedCartViewModel : ViewModel() {
    var itemList: MutableList<FoodItem> = mutableListOf()
    var restaurantID: String? = null
    var itemCount: Int = 0
    var currentOrder: Boolean = false
    //var order: Order?=null
    var restaurantIDtemp: String? = null
    var orderIDtemp: String? = null
    var creditCardNumber: String? = null
    var mmExpiration: String?= null
    var yyExpiration: String ?= null
    var cvvCode: String ?= null
    var nameOnCard: String ?= null
    var cardAdded: Boolean = false
    var studentIDselected: Boolean = false
    var driverEarnings: String? = null

    init {
        Log.i("SharedCartViewModel", "SharedCartViewModel created!")
        resetOrder()
    }

    fun setCreditCardInfo(card: String, mm: String, yy: String, cvv: String, name: String) {
        creditCardNumber = card
        mmExpiration = mm
        yyExpiration = yy
        cvvCode = cvv
        nameOnCard = name
    }
    private fun resetOrder() {
        itemList.clear()
    }

    fun getList(): MutableList<FoodItem> {
        return itemList
    }

    fun getCount(): Int {
        return itemList.size
    }

    fun addItem(newItem: FoodItem) {
        itemList.add(newItem)
    }

    fun getItem(i: Int): FoodItem{
        return itemList[i]
    }
    fun getDescription(i: Int): String {
        return this.itemList[i].description.toString()
    }
    fun getImg(i: Int): String {
        return this.itemList[i].img.toString()
    }
    fun getName(i: Int): String {
        return this.itemList[i].name.toString()
    }
    fun getPrice(i: Int): String {
        return this.itemList[i].price.toString()
    }
    fun getQuantity(i: Int): String {
        return this.itemList[i].quantity.toString()
    }
    fun getTotalItems(): Int {
        var total = 0
        itemList.forEach {
            total += it.quantity.toInt()
            Log.e(ContentValues.TAG, "++++++++++++++++++++++++: ${it.quantity.toInt()}")
        }
        return total
    }
    fun deleteItem(i: Int) {
       this.itemList[i]
    }

}