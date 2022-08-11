package com.dormdash.android.database

class Order {
    var firstName :String? = null
    var lastName :String? = null
    var userID :String? = null
    var phoneNum:String? =null
    var shippingAddress :String? = null
    var lat :Double = 0.toDouble()
    var lng :Double = 0.toDouble()
    var totalPayment :String? = null
    var restaurantID :String? = null
    var cartList: ArrayList<FoodItem>? = null
    var totalItems: Int? = null
    var orderID: String? = null
    var creditCardNum: String? = null
    var mmExpiration: String? = null
    var yyExpiration: String? = null
    var cvvCode: String? = null
    var nameOnCard: String? = null
    var studentID: String? = null
    var driverEarnings: String? = null

    constructor(){}

    constructor(
        firstName : String,
        lastName : String,
        userID : String,
        phoneNum : String,
        shippingAddress : String,
        lat : Double,
        lng : Double,
        totalPayment : String,
        restaurantID : String,
        cartList :  ArrayList<FoodItem>?,
        totalItems : Int,
        orderID : String,
        creditCardNum: String,
        mmExpiration: String,
        yyExpiration: String,
        cvvCode: String,
        nameOnCard: String,
        studentID: String,
        driverEarnings: String
    ){
        this.firstName = firstName
        this.lastName = lastName
        this.userID = userID
        this.phoneNum = phoneNum
        this.shippingAddress = shippingAddress
        this.lat = lat
        this.lng  = lng
        this.totalPayment  = totalPayment
        this.restaurantID = restaurantID
        this.cartList  = cartList
        this.totalItems = totalItems
        this.orderID = orderID
        this.creditCardNum = creditCardNum
        this.mmExpiration = mmExpiration
        this.yyExpiration = yyExpiration
        this.cvvCode = cvvCode
        this.nameOnCard = nameOnCard
        this.studentID= studentID
        this.driverEarnings = driverEarnings
    }
}