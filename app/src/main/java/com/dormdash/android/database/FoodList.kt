package com.dormdash.android.database

class FoodList {
    var foodName :String? = null
    var foodDescription:String? = null
    var foodImg :String? = null
    var foodPrice :String? = null
    var foodCal : Int? = null
    constructor(){}

    constructor(
        foodName: String?,
        foodDescription:String?,
        foodImg :String?,
        foodPrice :String?,
        foodCal :Int?

    ){
        this.foodName = foodName
        this.foodDescription = foodDescription
        this.foodImg = foodImg
        this.foodPrice = foodPrice
        this.foodCal = foodCal
    }
}