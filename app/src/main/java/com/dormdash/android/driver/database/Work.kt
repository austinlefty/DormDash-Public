package com.dormdash.android.driver.database

class Work {
    var lat :Double = 0.toDouble()
    var lng :Double = 0.toDouble()
    var driverLat :Double = 0.toDouble()
    var driverLng :Double = 0.toDouble()
    var driverUid : String? = null
    var orderID :String? = null
    var restaurantID : String? = null
    var status : Int? = null
    var restaurantLat : Double = 0.toDouble()
    var restaurantLng : Double = 0.toDouble()
    constructor(){}

    constructor(
        lat : Double,
        lng : Double,
        driverLat :Double,
        driverLng :Double,
        driverUid : String,
        orderID :String,
        restaurantID : String,
        status : Int,
        restaurantLat : Double,
        restaurantLng : Double,
    ) {
        this.lat = lat
        this.lng  = lng
        this.driverLat = driverLat
        this.driverLng = driverLng
        this.driverUid = driverUid
        this.orderID = orderID
        this.restaurantID = restaurantID
        this.status = status
        this.restaurantLat = restaurantLat
        this.restaurantLng = restaurantLng
    }
}

//package com.dormdash.android.driver.database
//
//class Work {
//    var lat :Double = 0.toDouble()
//    var lng :Double = 0.toDouble()
//    var driverUid : String? = null
//    var orderID :String? = null
//    var restaurantID : String? = null
//    var arrivedToRestaurant : String? = null
//    var arrivedToClient : String? = null
//    constructor(){}
//
//    constructor(
//        lat : Double,
//        lng : Double,
//        driverUid : String,
//        orderID :String,
//        restaurantID : String,
//        arrivedToRestaurant : String,
//        arrivedToClient : String
//    ) {
//        this.lat = lat
//        this.lng  = lng
//        this.driverUid = driverUid
//        this.orderID = orderID
//        this.restaurantID = restaurantID
//        this.arrivedToRestaurant = arrivedToRestaurant
//        this.arrivedToClient = arrivedToClient
//    }
//}