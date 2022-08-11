package com.dormdash.android.driver.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentDirectionsBinding
import com.dormdash.android.driver.bottomsheetbehavior.RestaurantArrivalConfirmationFragment
import com.dormdash.android.driver.viewmodel.DriverViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


class DirectionsFragment : Fragment() , OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private lateinit var navController: NavController
    private val driverViewModel : DriverViewModel by activityViewModels()
    private var latLngOrigin: LatLng? = null
    private var latLngWayPoint: LatLng? = null
    private var latLngDestination: LatLng? = null
    private var mode = "w"
    var totalDistance = 0.0
    var totalDuration = 0
    private var fragmentDirectionsBinding : FragmentDirectionsBinding? = null
    private var modalBottomSheetFragment: RestaurantArrivalConfirmationFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDirectionsBinding.inflate(inflater, container, false)
        fragmentDirectionsBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(ContentValues.TAG, "*******************Times ONVIEWCREATE:*************: $000")
        navController = Navigation.findNavController(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
//        openDialog()
        getDirection("driving")
        fragmentDirectionsBinding!!.startDirectionsBtn.setOnClickListener { openGoogleMaps() }
        fragmentDirectionsBinding!!.travelMode.setOnCheckedChangeListener { _, checked ->
            if (checked != -1) {
                when (checked) {
                    R.id.btnChipDriving -> {
                        mode = "d"
                        driverViewModel.mode = "d"
                        getDirection("driving")
                    }
                    R.id.btnChipWalking -> {
                        mode = "w"
                        driverViewModel.mode = "w"
                        getDirection("walking")
                    }
                    R.id.btnChipBike -> {
                        mode = "b"
                        driverViewModel.mode = "b"
                        getDirection("bicycling")
                    }
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        Log.e(ContentValues.TAG, "*******************On Resume Dialog*************")
//        openDialog()
//    }
//
//    private fun openDialog() {
//        Log.e(ContentValues.TAG, "*******************On Resume Dialog*************")
//        if (driverViewModel.status == 2 ){
//            parentFragmentManager.let { modalBottomSheetFragment!!.show(it, modalBottomSheetFragment!!.tag) }
//        }
//    }

    private fun openGoogleMaps() {
        modalBottomSheetFragment = RestaurantArrivalConfirmationFragment(context)
        driverViewModel.status = 3
        fragmentDirectionsBinding!!.startDirectionsBtn.visibility = View.GONE
        parentFragmentManager.let { modalBottomSheetFragment!!.show(it, modalBottomSheetFragment!!.tag) }
        val gmmIntentUri = Uri.parse("google.navigation:q=${latLngWayPoint!!.latitude},${latLngWayPoint!!.longitude}" + "&mode=" + mode)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun getDirection(travelMode: String) {
        latLngOrigin = LatLng(driverViewModel.location!!.latitude , driverViewModel.location!!.longitude) // Current Location
        latLngWayPoint = LatLng(driverViewModel.currentTask!!.restaurantLat , driverViewModel.currentTask!!.restaurantLng) // Restaurant Location Moes
        // this is the right one but i hard coded for testing
        val latLngDestination = LatLng(driverViewModel.currentTask!!.lat, driverViewModel.currentTask!!.lng)
        //latLngDestination = LatLng(41.77076727099191, -88.14546181959403) // Destination NCC

        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + latLngOrigin!!.latitude + "," + latLngOrigin!!.longitude +
                "&destination=" + latLngWayPoint!!.latitude + "," + latLngWayPoint!!.longitude +
                "&waypoints=" + latLngDestination!!.latitude + "," + latLngDestination!!.longitude +
                "&mode=" + travelMode +
                "&key=AIzaSyCImeQ9QSl-vmQIAzTmiQJP6h_0yIi7Ccc"

        clearUI()
        val directionsRequest = @SuppressLint("SetTextI18n")
        object : StringRequest(Request.Method.GET, urlDirections, Response.Listener {
                response ->
            val jsonResponse = JSONObject(response)
            Log.e(ContentValues.TAG, "*******************Route Response:*************: $jsonResponse")
            Log.e(ContentValues.TAG, "*******************Route Response:*************: ${jsonResponse.getJSONArray("routes")}")
            Log.e(ContentValues.TAG, "*******************Route Response:*************: ${jsonResponse.getJSONArray("routes")[0]}")
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            if (travelMode.contentEquals("walking")){
                drawToRestaurant(routes, true)
                drawToDestination(routes, true)
            }else{
                drawToRestaurant(routes, false)
                drawToDestination(routes, false)
            }

            googleMap?.addMarker(MarkerOptions().position(latLngOrigin!!).title("You"))
            googleMap?.addMarker(MarkerOptions().position(latLngWayPoint!!).title("Moe's Food Pick Up"))
            googleMap?.addMarker(MarkerOptions().position(latLngDestination!!).title("Destination"))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngWayPoint!!, 13.5f))




            fragmentDirectionsBinding!!.totalDistance.text = "$totalDistance mi"
            fragmentDirectionsBinding!!.totalEta.text = "$totalDuration mins"



        }, Response.ErrorListener {
                e ->  Log.e(ContentValues.TAG, "*******************Map Error:*************: $e")
        }){}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)

    }

    private fun clearUI() {
        googleMap?.clear()
        totalDistance = 0.0
        totalDuration = 0
        fragmentDirectionsBinding!!.totalDistance.text = ""
        fragmentDirectionsBinding!!.totalEta.text = ""
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // Sample coordinates

//        val latLngOrigin = LatLng(41.79115994093222, -88.14919074830601) // Current Location
//        val latLngWayPoint = LatLng(41.789118945822686, -88.13209502310534) // Restaurant Location Portillo
//        val latLngDestination = LatLng(41.7731035461633, -88.14261741097977) // Destination NCC
//        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("You"))
//        this.googleMap!!.addMarker(MarkerOptions().position(latLngWayPoint).title("Food Pick Up"))
//        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("Home"))
//        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
//        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=41.79115994093222, -88.14919074830601&destination=41.7731035461633, -88.14261741097977&waypoints=41.789118945822686,-88.13209502310534&key=AIzaSyCImeQ9QSl-vmQIAzTmiQJP6h_0yIi7Ccc"
//        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener {
//            response ->
//            val jsonResponse = JSONObject(response)
//            Log.e(ContentValues.TAG, "*******************Route Response:*************: $jsonResponse")
//            Log.e(ContentValues.TAG, "*******************Route Response:*************: ${jsonResponse.getJSONArray("routes")}")
//            Log.e(ContentValues.TAG, "*******************Route Response:*************: ${jsonResponse.getJSONArray("routes")[0]}")
//            // Get routes
//            val routes = jsonResponse.getJSONArray("routes")
//
//            drawToRestaurant(routes)
//            drawToDestination(routes)

//
//            fragmentDirectionsBinding!!.totalDistance.text = totalDistance.toString()
//            fragmentDirectionsBinding!!.totalEta.text = totalDuration.toString()


//
//        }, Response.ErrorListener {
//                        e ->  Log.e(ContentValues.TAG, "*******************Map Error:*************: $e")
//        }){}
//        val requestQueue = Volley.newRequestQueue(context)
//        requestQueue.add(directionsRequest)
    }

    private fun drawToDestination(routes: JSONArray, b: Boolean) {
        val paths: MutableList<List<LatLng>> = ArrayList()
        val legs = routes.getJSONObject(0).getJSONArray("legs")

        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")
        val subTextDuration = duration.toString().substringBefore(" ")
        val durationInt = subTextDuration.toInt()
        totalDuration += durationInt
        Log.e(ContentValues.TAG, "*******************toDestination1:*************: $totalDuration")

        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
        val subTextDistance = distance.toString().substringBefore(" ")
        val distanceFloat = subTextDistance.toDouble()
        totalDistance += distanceFloat
        Log.e(ContentValues.TAG, "*******************toDestination2:*************: $totalDistance")

        val steps = legs.getJSONObject(1).getJSONArray("steps")
        for (i in 0 until steps.length()) {
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            paths.add(PolyUtil.decode(points))
        }
        val options = PolylineOptions().apply {
            width(10f)
            color(Color.BLUE)
            geodesic(true)
            clickable(true)
            visible(true)
        }

        val pattern: List<PatternItem>

        if (b) {
            pattern = listOf(Dot(), Gap(10f))
            options.jointType(JointType.ROUND)
        } else {
            pattern = listOf(Dash(20f))
        }
        options.pattern(pattern)
        for (i in 0 until paths.size) {
            this.googleMap!!.addPolyline(options.addAll(paths[i]))
        }
    }

    private fun drawToRestaurant(routes: JSONArray, b: Boolean) {
        val path: MutableList<List<LatLng>> = ArrayList()
        val legs = routes.getJSONObject(0).getJSONArray("legs")

        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")
        val subTextDuration = duration.toString().substringBefore(" ")
        val durationInt = subTextDuration.toInt()
        totalDuration += durationInt
        Log.e(ContentValues.TAG, "*******************drawToRestaurant1:*************: $totalDuration")

        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
        val subTextDistance = distance.toString().substringBefore(" ")
        val distanceFloat = subTextDistance.toDouble()
        totalDistance += distanceFloat
        Log.e(ContentValues.TAG, "*******************drawToRestaurant2:*************: $totalDistance")

        val steps = legs.getJSONObject(0).getJSONArray("steps")
        for (i in 0 until steps.length()) {
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            path.add(PolyUtil.decode(points))
        }
        val options = PolylineOptions().apply {
            width(10f)
            color(Color.BLUE)
            geodesic(true)
            clickable(true)
            visible(true)
        }

        val pattern: List<PatternItem>

        if (b) {
            pattern = listOf(Dot(), Gap(10f))
            options.jointType(JointType.ROUND)
        } else {
            pattern = listOf(Dash(20f))
        }
        options.pattern(pattern)
        for (i in 0 until path.size) {
            this.googleMap!!.addPolyline(options.addAll(path[i]))
        }
    }
}