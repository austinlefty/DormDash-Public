package com.dormdash.android.driver.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentDriverHomePageBinding
import com.dormdash.android.driver.bottomsheetbehavior.DriverHomeBottomSheetFragment
import com.dormdash.android.driver.database.Work
import com.dormdash.android.driver.viewmodel.DriverViewModel
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.set

class DriverHomePageFragment : Fragment() , LocationListener, View.OnClickListener ,
    OnMapReadyCallback {
    private var fragmentDriverHomePageBinding: FragmentDriverHomePageBinding? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private var googleMap: GoogleMap? = null
    private lateinit var navController: NavController
    private lateinit var toDataBase: DatabaseReference
    private lateinit var fromDataBase: DatabaseReference
    private lateinit var activeDataBase: DatabaseReference
    private lateinit var workDataBase: DatabaseReference
    private var latLngOrigin: LatLng? = null
    private lateinit var workList : ArrayList<Work>
    private val driverViewModel : DriverViewModel by activityViewModels()
    private val viewModel : CurrentUserSharedViewModel by activityViewModels()

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocationUpdate: Location
    private lateinit var currentLocation: Location
    private lateinit var locationCallback: LocationCallback
    private var modalBottomSheetFragment: DriverHomeBottomSheetFragment? = null
    private var status = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = FragmentDriverHomePageBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentDriverHomePageBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "DriverHomePageFragment******: ")
        navController = Navigation.findNavController(view)
        modalBottomSheetFragment = DriverHomeBottomSheetFragment()
        fragmentDriverHomePageBinding!!.backBtn.setOnClickListener(this)
        workList = ArrayList()
        initLocation()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        setSwitch()
    }


    private fun removeTask() {
    }


    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallBack()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                updateFireBaseLocation()
            }
        }
    }


    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
            smallestDisplacement = 50f
        }
    }


    private fun setSwitch() {
//        fragmentDriverHomePageBinding!!.switch1.isChecked = driverViewModel.isChecked
        fragmentDriverHomePageBinding!!.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && driverViewModel.status == 1){ // active
                changeStatusToActive()
                loadTask()
                status = true
            }
            else if (isChecked && driverViewModel.status > 1){
                Log.e(ContentValues.TAG, "*******************ALRLEADY HAVE A JOB*************")
//                modalBottomSheetFragment!!.dialog!!.show()
            }
            else{ // offline
                changeStatusToOffline()
                status = false
            }
        }
    }

    private fun loadTask() {
        workDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Assigned/${viewModel.currentUser!!.uid!!}")
        workDataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    driverViewModel.currentTask = snapshot.getValue(Work::class.java)
                    workList.add(driverViewModel.currentTask!!)
//                    updateFireBaseLocation()
                    loadMap()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadTaskInfo() {
        if (!modalBottomSheetFragment!!.isAdded && driverViewModel.status == 1 ){
            modalBottomSheetFragment = DriverHomeBottomSheetFragment()
            Log.e(TAG, "*******************DIALOG IS NOTTT ADDED:*************: ${driverViewModel.totalDistance}")
            parentFragmentManager.let { modalBottomSheetFragment!!.show(it, modalBottomSheetFragment!!.tag) }
            Log.e(TAG, "*******************CHECKING NAV UNDER IS ADDED:*************: ${driverViewModel.totalDistance}")
        }
        if (modalBottomSheetFragment!!.isAdded && driverViewModel.status > 1 ){
            modalBottomSheetFragment!!.dialog!!.dismiss()
        }

        Log.e(TAG, "*******************DIALOG IS ADDED:*************: ${driverViewModel.totalDistance}")
    }


    private fun removeFromActive() {
        activeDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Active/${viewModel.currentUser!!.uid!!}")
        activeDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                else {
                    activeDataBase.removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun changeStatusToActive() {
        toDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Active/${viewModel.currentUser!!.uid!!}")
        toDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    val updateData = HashMap<String, Any>()
//                    updateData["active"] = "true"
                    updateData["driverUid"] = viewModel.currentUser!!.uid.toString()
                    toDataBase
                        .updateChildren(updateData)
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                    updateFireBaseLocation()
                    removeFromOffline()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun removeFromOffline() {
        activeDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Offline/${viewModel.currentUser!!.uid!!}")
        activeDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                else {
                    activeDataBase.removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun changeStatusToOffline() {
        fromDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Offline/${viewModel.currentUser!!.uid!!}")
        fromDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    val updateData = HashMap<String, Any>()
                    updateData["active"] = "false"
                    fromDataBase
                        .updateChildren(updateData)
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                    removeFromActive()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    override fun onResume() {
        super.onResume()
        Log.e(ContentValues.TAG, "*******************DHPR ON RESUME:*************")

        startLocationUpdates()
    }


    private fun startLocationUpdates() {
        initLocation()
    }


    override fun onLocationChanged(currentLocation: Location) {
        currentLocationUpdate = currentLocation

        //updateFireBaseLocation()
    }


    private fun updateFireBaseLocation() {
        val updateData = HashMap<String, Any>()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (status){
            fusedLocationProviderClient.lastLocation
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "" + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnCompleteListener { task ->
                    driverViewModel.location = task.result
                    updateData["lat"] = task.result.latitude
                    updateData["lng"] = task.result.longitude
                    driverViewModel.location!!.latitude = task.result.latitude
                    driverViewModel.location!!.longitude = task.result.longitude
                    Log.e(TAG, "*******************current Location3:*************: ${driverViewModel.location!!.latitude}")
                    Log.e(TAG, "*******************current Location4:*************: ${driverViewModel.location!!.longitude}")
                    FirebaseDatabase.getInstance()
                        .getReference("Drivers/Active")
                        .child(viewModel.currentUser!!.uid!!.toString() )
                        .updateChildren(updateData)
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.accept_work_btn -> {
                Log.e(ContentValues.TAG, "*******************accept_work_btn*************")
                modalBottomSheetFragment!!.dismiss()
                navController.navigate(R.id.action_driverHomePageFragment_to_directionsFragment)
            }

            R.id.decline_work_btn -> {
                Log.e(ContentValues.TAG, "*******************decline_work_btn *************")
                modalBottomSheetFragment!!.dismiss()
            }
            R.id.back_btn -> {
                navController.navigate(R.id.action_driverHomePageFragment_to_homePage)
            }
        }
    }


    private fun loadMap() {
        if (driverViewModel.status == 1 && driverViewModel.currentTask!= null){
        latLngOrigin = LatLng(driverViewModel.location!!.latitude , driverViewModel.location!!.longitude) // Current Location
        val latLngWayPoint = LatLng(driverViewModel.currentTask!!.restaurantLat , driverViewModel.currentTask!!.restaurantLng) // Restaurant Location Moes
        // this is the right one but i hard coded for testing
        val latLngDestination = LatLng(driverViewModel.currentTask!!.lat, driverViewModel.currentTask!!.lng)
        //val latLngDestination = LatLng(41.77076727099191, -88.14546181959403) // Destination NCC

        googleMap?.addMarker(MarkerOptions().position(latLngWayPoint).title("Moe's Food Pick Up"))
        googleMap?.addMarker(MarkerOptions().position(latLngDestination).title("Destination"))
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + latLngOrigin!!.latitude + "," + latLngOrigin!!.longitude +
                "&destination=" + latLngWayPoint.latitude + "," + latLngWayPoint.longitude +
                "&waypoints=" + latLngDestination.latitude + "," + latLngDestination.longitude +
                "&key=AIzaSyCImeQ9QSl-vmQIAzTmiQJP6h_0yIi7Ccc"
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngWayPoint, 13.5f))
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener {
                response ->
            driverViewModel.jsonResponse = JSONObject(response)
            Log.e(TAG, "*******************Route Response:*************: ${driverViewModel.jsonResponse}")
            Log.e(TAG, "*******************Route Response:*************: ${driverViewModel.jsonResponse!!.getJSONArray("routes")}")
            Log.e(TAG, "*******************Route Response:*************: ${driverViewModel.jsonResponse!!.getJSONArray("routes")[0]}")
            // Get routes
            val routes = driverViewModel.jsonResponse!!.getJSONArray("routes")


                drawToRestaurant(routes)
                drawToDestination(routes)
                loadTaskInfo()



            // make the dialog here **************************************

//            fragmentDirectionsBinding!!.totalDistance.text = totalDistance.toString()
//            fragmentDirectionsBinding!!.totalEta.text = totalDuration.toString()

        }, Response.ErrorListener {
                e ->  Log.e(TAG, "*******************Map Error:*************: $e")
        }){}
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(directionsRequest)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.e(TAG, "*******************Map Error:*************: TIMES MAKING YOU MARKER")
        this.googleMap = googleMap
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "" + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener { task ->
                driverViewModel.location = task.result
                Log.e(TAG, "*******************current Location111:*************: ${driverViewModel.location!!.latitude}")
                Log.e(TAG, "*******************current Location222:*************: ${driverViewModel.location!!.longitude}")

                latLngOrigin = LatLng(driverViewModel.location!!.latitude , driverViewModel.location!!.longitude) // Current Location
                this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin!!).title("You"))
                this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin!!, 12f))
            }
    }


    private fun drawToDestination(routes: JSONArray) {
        val paths: MutableList<List<LatLng>> = ArrayList()
        val legs = routes.getJSONObject(0).getJSONArray("legs")

        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")
        val subTextDuration = duration.toString().substringBefore(" ")
        val durationInt = subTextDuration.toInt()
        driverViewModel.totalDuration += durationInt
        Log.e(TAG, "*******************toDestination1:*************: ${driverViewModel.totalDuration}")

        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
        val subTextDistance = distance.toString().substringBefore(" ")
        val distanceFloat = subTextDistance.toDouble()
        driverViewModel.totalDistance  += distanceFloat
        Log.e(TAG, "*******************toDestination2:*************: ${driverViewModel.totalDistance}")

        val steps = legs.getJSONObject(1).getJSONArray("steps")
        for (i in 0 until steps.length()) {
            Log.e(TAG, "*******************in Dest loop1 :*************: $i")
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            paths.add(PolyUtil.decode(points))
        }
        for (i in 0 until paths.size) {
            Log.e(TAG, "*******************in Dest loop2 :*************: $i")
            this.googleMap!!.addPolyline(PolylineOptions().addAll(paths[i]).color(Color.RED))
        }
    }


    private fun drawToRestaurant(routes: JSONArray) {
        val path: MutableList<List<LatLng>> = ArrayList()
        val legs = routes.getJSONObject(0).getJSONArray("legs")

        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")
        val subTextDuration = duration.toString().substringBefore(" ")
        val durationInt = subTextDuration.toInt()
        driverViewModel.totalDuration += durationInt
        Log.e(TAG, "*******************drawToRestaurant1:*************: ${driverViewModel.totalDuration}")

        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
        val subTextDistance = distance.toString().substringBefore(" ")
        val distanceFloat = subTextDistance.toDouble()
        driverViewModel.totalDistance += distanceFloat
        Log.e(TAG, "*******************drawToRestaurant2:*************: ${driverViewModel.totalDistance}")

        val steps = legs.getJSONObject(0).getJSONArray("steps")
        for (i in 0 until steps.length()) {
            Log.e(TAG, "*******************in Rest loop1 :*************: $i")
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            path.add(PolyUtil.decode(points))
        }
        for (i in 0 until path.size) {
            Log.e(TAG, "*******************in Rest loop2 :*************: $i")
            this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
        }
    }
}