package com.dormdash.android.driver.bottomsheetbehavior

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentRestaurantArrivalConfirmationBinding
import com.dormdash.android.driver.viewmodel.DriverViewModel
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*


class RestaurantArrivalConfirmationFragment(directionsFragment: Context?) : BottomSheetDialogFragment(), View.OnClickListener {
    private var dirContext = directionsFragment
    private var fragmentRestaurantArrivalConfirmationFragmentBinding: FragmentRestaurantArrivalConfirmationBinding? = null
    private val driverViewModel : DriverViewModel by activityViewModels()
    private val viewModel : CurrentUserSharedViewModel by activityViewModels()
    private var driverEarningsBottomSheet: DriverEarningsBottomSheetFragment ?= null

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocationUpdate: Location
    private lateinit var currentLocation: Location

    private lateinit var toDataBase: DatabaseReference
    private lateinit var assignedDataBase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentRestaurantArrivalConfirmationBinding.inflate(inflater, container, false)
        fragmentRestaurantArrivalConfirmationFragmentBinding = binding
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocation()
        dialog!!.setCancelable(false)
        dialog!!.setCanceledOnTouchOutside(false)
        driverEarningsBottomSheet = DriverEarningsBottomSheetFragment()
        fragmentRestaurantArrivalConfirmationFragmentBinding!!.arrivedAtRestaurantBtn.setOnClickListener(this)
        fragmentRestaurantArrivalConfirmationFragmentBinding!!.startCustomerNavigationBtn.setOnClickListener(this)
        fragmentRestaurantArrivalConfirmationFragmentBinding!!.confirmDroppedFoodBtn.setOnClickListener(this)
        fragmentRestaurantArrivalConfirmationFragmentBinding!!.googleMaps.setOnClickListener(this)
        setUpUI()
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


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }


    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }


    private fun startLocationUpdates() {
        initLocation()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }


    private fun openGoogleMaps() {
        // if status == -1 then open maps to restaurant
        // status = 0
        // if status < 1  then open maps to customer
        // status = 1
        if (driverViewModel.status < 5){
            Log.e(ContentValues.TAG, "*******************open Google Maps 1:*************")
//            driverViewModel.status = 1 // driver started and on its way
            val latLngWayPoint = LatLng(driverViewModel.currentTask!!.restaurantLat , driverViewModel.currentTask!!.restaurantLng)
            val gmmIntentUri = Uri.parse("google.navigation:q=${latLngWayPoint.latitude},${latLngWayPoint.longitude}" + "&mode=" + driverViewModel.mode)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        else if (driverViewModel.status > 4){
            Log.e(ContentValues.TAG, "*******************open Google Maps 2*************")
//            driverViewModel.status = 2 // driver started and on its way
            val latLngDestination = LatLng(driverViewModel.currentTask!!.lat, driverViewModel.currentTask!!.lng)
            //val latLngDestination = LatLng(41.77076727099191, -88.14546181959403) // Destination NCC
            val gmmIntentUri = Uri.parse("google.navigation:q=${latLngDestination.latitude},${latLngDestination.longitude}" + "&mode=" + driverViewModel.mode)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun setUpUI() {
        when (driverViewModel.status ) {
            4 -> fragmentRestaurantArrivalConfirmationFragmentBinding!!.arrivedAtRestaurantBtn.isEnabled = false
            5 -> {
                fragmentRestaurantArrivalConfirmationFragmentBinding!!.arrivedAtRestaurantBtn.isEnabled = false
                fragmentRestaurantArrivalConfirmationFragmentBinding!!.startCustomerNavigationBtn.isEnabled = false
            }
            6 -> {
                // task is complete so now we go back and listen for new jobs
                fragmentRestaurantArrivalConfirmationFragmentBinding!!.arrivedAtRestaurantBtn.isEnabled = false
                fragmentRestaurantArrivalConfirmationFragmentBinding!!.startCustomerNavigationBtn.isEnabled = false
                fragmentRestaurantArrivalConfirmationFragmentBinding!!.googleMaps.isEnabled = false
                Toast.makeText(context, "  Task completed23", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.arrived_at_restaurant_btn -> {
                Toast.makeText(context, " status Arrived", Toast.LENGTH_LONG).show()
                driverViewModel.status = 4
                setUpUI()
                updateFireBase()
            }
            R.id.start_customer_navigation_btn -> {
                Toast.makeText(context, "  status Started nav to customer", Toast.LENGTH_LONG).show()
                driverViewModel.status = 5
                setUpUI()
                updateFireBase()
                openGoogleMaps()
            }
            R.id.confirm_dropped_food_btn -> {
                Toast.makeText(context, "  Confirmed dropped off", Toast.LENGTH_LONG).show()
                driverViewModel.status = 6
                setUpUI()
                //driverEarningsBottomSheet!!.dialog!!.show()
                removeAssignedTasked()
                updateFireBase()
                findNavController().navigate(R.id.action_directionsFragment_to_driverHomePageFragment)
                dismiss()
            }
            R.id.google_maps -> {
                Toast.makeText(context, "  status none open google maps", Toast.LENGTH_LONG).show()
                openGoogleMaps()
            }
        }
    }

    private fun updateFireBase() {
        val updateData = HashMap<String, Any>()
        updateData["status"] = driverViewModel.status
        Log.e(ContentValues.TAG, "******************* Driver Status = 1:*************: ${driverViewModel.status}")
        FirebaseDatabase.getInstance()
            .getReference("Drivers/Assigned/${viewModel.currentUser!!.uid!!}")
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener { Log.e(ContentValues.TAG, "******************* PASSED = 88:*************: $it")
            }
    }

    private fun removeAssignedTasked() {
        removeOrderFromRestaurant()
        changeStatusToActive()
    }

    private fun removeOrderFromRestaurant() {
        // would like to move order to restaurants order history and delete it after 5+ business days
        val path = "Restaurants/" +
                "${driverViewModel.currentTask!!.restaurantID}/" +
                "Orders/" + "${driverViewModel.currentTask!!.orderID}"
        Log.e(ContentValues.TAG, "Path: $path")
        assignedDataBase = FirebaseDatabase.getInstance().getReference(path)
        assignedDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                else {
                    assignedDataBase.removeValue()
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                        .addOnSuccessListener { Log.e(ContentValues.TAG, "******************* removeOrderFromRestaurant() PASSED = 999:*************: $it")
                            driverViewModel.isChecked = false
                            driverViewModel.status = 1
                            driverViewModel.currentTask = null
                            driverViewModel.totalDuration = 0
                            driverViewModel.totalDistance = 0.0

                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun changeStatusToActive() {
        toDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Active/${viewModel.currentUser!!.uid!!}")
        toDataBase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    val updateData = HashMap<String, Any>()
//                    updateData["active"] = "true"
                    updateData["driverUid"] = viewModel.currentUser!!.uid.toString()
                    toDataBase
                        .updateChildren(updateData)
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                        .addOnSuccessListener { Log.e(ContentValues.TAG, "******************* changeStatusToActive() PASSED = 99:*************: $it")
                            removeFromAssigned()
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun removeFromAssigned() {
        Log.e(ContentValues.TAG, "******************* Started Remove From Assigned:*************:")
        assignedDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Assigned/${viewModel.currentUser!!.uid!!}")
        assignedDataBase.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                else {
                    Log.e(ContentValues.TAG, "******************* Started Remove From Assigned1:*************:")
                    assignedDataBase.removeValue()
                    Log.e(ContentValues.TAG, "******************* Started Remove From Assigned2:*************:")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateFireBaseLocation() {
        val updateData = HashMap<String, Any>()
        if (ActivityCompat.checkSelfPermission(
                dirContext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                dirContext!!,
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
//        if (status){
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
                    Log.e(ContentValues.TAG, "*******************current Location3:*************: ${driverViewModel.location!!.latitude}")
                    Log.e(ContentValues.TAG, "*******************current Location4:*************: ${driverViewModel.location!!.longitude}")
                    FirebaseDatabase.getInstance()
                        .getReference("Drivers/Assigned")
                        .child(viewModel.currentUser!!.uid!!.toString() )
                        .updateChildren(updateData)
                        .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
                        .addOnSuccessListener { Log.e(ContentValues.TAG, "******************* PASSED = 77:*************: $it")
                        }
                }
//        }
    }
}