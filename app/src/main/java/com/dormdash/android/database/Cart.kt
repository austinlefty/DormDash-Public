package com.dormdash.android.database

class Cart {
    var name :String? = null
    var img:String? = null
    var price :String? = null
    var quantity : String? = null
    var description : String? = null
    constructor(){}

    constructor(
        name : String,
        img : String,
        price : String,
        quantity : String,
        description : String,

    ){
        this.name = name
        this.img = img
        this.price = price
        this.quantity  = quantity
        this.description  = description
    }
    fun deletItemIncart(){}
}