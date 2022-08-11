package com.dormdash.android.driver.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import com.dormdash.android.driver.database.Work
import org.json.JSONObject

class DriverViewModel: ViewModel() {
    var currentTask: Work? = null
    var jsonResponse: JSONObject? = null
    var location: Location? = null
    val restaurantLocation: Location? = null
    val deliveryLocation: Location? = null
    var totalDistance = 0.0
    var totalDuration = 0
    var status = 1
    var isChecked = false
    var mode = "d"

}