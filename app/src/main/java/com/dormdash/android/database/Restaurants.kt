package com.dormdash.android.database

class Restaurants {
    var nameRestaurants :String? = null
    var deliveryFee :String? = null
    var img :String? = null
    var address :String? = null
    var eta :String? = null
    var menuID :String? = null
    constructor(){}

    constructor(
        nameRestaurants: String?,
        deliveryFee:String?,
        img :String?,
        address :String?,
        eta :String?,
        menuID :String? = null
    ){
        this.nameRestaurants = nameRestaurants
        this.deliveryFee = deliveryFee
        this.img = img
        this.address = address
        this.eta = eta
        var menuID = menuID
    }
}