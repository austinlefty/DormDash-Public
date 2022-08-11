package com.dormdash.android.driver.database

class Driver {
    var lat :Double = 0.toDouble()
    var lng :Double = 0.toDouble()
    var driverUid : String? = null
    constructor(){}

    constructor(
        lat : Double,
        lng : Double,
        driverUid : String,
    ) {
        this.lat = lat
        this.lng  = lng
        this.driverUid = driverUid
    }
}