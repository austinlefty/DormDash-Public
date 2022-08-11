package com.dormdash.android.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.dormdash.android.R
import com.dormdash.android.adapter.PaymentAdapter
import com.dormdash.android.database.FoodItem
import com.dormdash.android.database.Order
import com.dormdash.android.databinding.FragmentFoodOrderConfirmationBinding
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random


class FoodOrderConfirmationFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cartList:ArrayList<FoodItem>
    private lateinit var mAdapter: PaymentAdapter
    private var fragmentFoodOrderConfirmationBinding: FragmentFoodOrderConfirmationBinding? = null
    private val viewModel : SharedCartViewModel by activityViewModels()
    private val studentModel : CurrentUserSharedViewModel by activityViewModels()
    private lateinit var order: Order

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var locationCallback: LocationCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // build location
        initLocation()
        // Inflate the layout for this fragment
        val binding = FragmentFoodOrderConfirmationBinding.inflate(inflater, container, false)
        fragmentFoodOrderConfirmationBinding = binding
        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallBack()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
            smallestDisplacement = 10f
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        //*************************** can put this in a function to clean it up **********************************************
        cartList = ArrayList()
        cartList = viewModel.getList() as ArrayList<FoodItem>
        var subPrice = 0.00
        viewModel.getList().forEach { subPrice += it.price.substring(1, it.price.length).toDouble() }
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val subPriceRounded = df.format(subPrice)
        updatePayment(subPriceRounded.substring(1, subPriceRounded.length))
        //*************************************************************************
        fragmentFoodOrderConfirmationBinding!!.subtotalNumberText.text = subPriceRounded.toString()
        mAdapter = PaymentAdapter(this, viewModel.getList() as ArrayList<FoodItem>, fragmentFoodOrderConfirmationBinding!!, navController, viewModel)
        fragmentFoodOrderConfirmationBinding!!.paymentCart.layoutManager = LinearLayoutManager(context)
        fragmentFoodOrderConfirmationBinding!!.paymentCart.adapter = mAdapter
        fragmentFoodOrderConfirmationBinding!!.paymentAddressButton.setOnClickListener(this)
        fragmentFoodOrderConfirmationBinding!!.paymentConfirmationButton.setOnClickListener {setOrder()}
    }

    private fun updatePayment(subPriceRounded: String) {
        val grandTotal = getDeliveryFee(subPriceRounded.toDouble()) +
                getServiceFee(subPriceRounded.toDouble()) +
                getTax(subPriceRounded.toDouble()) +
                subPriceRounded.toDouble()

        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.CEILING
        val rounded = df.format(grandTotal)
        fragmentFoodOrderConfirmationBinding!!.totalNumberText.text = rounded.toString()
    }

    private fun getTax(subPrice: Double): Double {
        val tax = 0.0625
        val totalTax = subPrice.times(tax)
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val rounded = df.format(totalTax)
        fragmentFoodOrderConfirmationBinding!!.taxNumberText.text = rounded.toString()
        return rounded.substring(1, rounded.length).toDouble()

    }

    private fun getServiceFee(subPrice: Double): Double {
        val fee = 0.15
        val totalFee = subPrice.times(fee)
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val rounded = df.format(totalFee)
        fragmentFoodOrderConfirmationBinding!!.serviceFeeNumberText.text = rounded.toString()
        return rounded.substring(1, rounded.length).toDouble()
    }

    private fun getDeliveryFee(subPrice: Double): Double {
        val fee = 0.3
        val totalFee = subPrice.times(fee)
        val df = DecimalFormat.getCurrencyInstance()
        df.roundingMode = RoundingMode.HALF_EVEN
        val rounded = df.format(totalFee)
        fragmentFoodOrderConfirmationBinding!!.deliveryFeeNumberText.text = rounded.toString()
        viewModel.driverEarnings = rounded.toString()
        return rounded.substring(1, rounded.length).toDouble()
    }

    private fun setOrder() {
        if (cartList.isEmpty()){
            Toast.makeText(requireContext(), "Cart is Empty" , Toast.LENGTH_SHORT).show()
        }
        else if (!viewModel.cardAdded) {
                Toast.makeText(requireContext(), "Please input your payment information.", Toast.LENGTH_SHORT).show()
        }
        else {
            if(viewModel.currentOrder)
                Toast.makeText(requireContext(), "An order is already in progress and must finish before a new one is made." , Toast.LENGTH_SHORT).show()
            else {
                viewModel.currentOrder = true
                order = Order()
                order.totalItems = viewModel.getTotalItems()
                order.orderID = createOrderNumber()
                order.restaurantID = viewModel.restaurantID
                order.firstName = studentModel.currentUser!!.firstName
                order.lastName = studentModel.currentUser!!.lastName
                order.userID = studentModel.currentUser!!.uid
                order.phoneNum = studentModel.currentUser!!.phoneNum
                order.cartList = cartList
                // need to calculate the total cart. like the fees and tax. until then i will use the subtotal
                order.totalPayment =
                    fragmentFoodOrderConfirmationBinding!!.totalNumberText.text.toString()
                order.creditCardNum = viewModel.creditCardNumber
                order.mmExpiration = viewModel.mmExpiration
                order.yyExpiration = viewModel.yyExpiration
                order.cvvCode = viewModel.cvvCode
                order.nameOnCard = viewModel.nameOnCard
                order.studentID = studentModel.currentUser!!.studentID
                order.driverEarnings = viewModel.driverEarnings

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

                fusedLocationProviderClient.lastLocation
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "" + e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnCompleteListener { task ->
                        order.lat = task.result.latitude
                        order.lng = task.result.longitude
                        order.shippingAddress = getAddress()
                        Log.e(
                            TAG,
                            "*******************ListCount in lat1*************: ${order.lat}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in lng1*************: ${order.lng}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in firstName*************: ${order.firstName}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in lastName*************: ${order.lastName}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in userUID*************: ${order.userID}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in phoneNum*************: ${order.phoneNum}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in shippingAddress*************: ${order.shippingAddress}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in totalPayment*************: ${order.totalPayment}"
                        )
                        Log.e(
                            TAG,
                            "*******************ListCount in totalItems*************: ${order.totalItems}"
                        )
                        writeOderToFirebase(order)
                    }
//            order.lat = fusedLocationProviderClient.lastLocation.result.latitude
//            order.lng = fusedLocationProviderClient.lastLocation.result.longitude
            }
        }
    }

    private fun writeOderToFirebase(order: Order) {
        FirebaseDatabase.getInstance()
            .getReference("Restaurants/${viewModel.restaurantID}/Orders")
            .child(order.orderID.toString() )
            .setValue(order)
            .addOnFailureListener { e -> Toast.makeText(requireContext(), ""+ e.message, Toast.LENGTH_LONG).show() }
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    cartList.clear()
                    Toast.makeText(requireContext(), "Order has been added!", Toast.LENGTH_LONG).show()
                    order.cartList!!.clear()
                    viewModel.itemCount = 0
                    viewModel.restaurantIDtemp = viewModel.restaurantID
                    viewModel.restaurantID = null
                    viewModel.orderIDtemp = order.orderID
                    navController.navigate(R.id.action_foodOrderConfirmationFragment_to_orderStatusFragment)
                }
            }


    }

    private fun createOrderNumber(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(abs(Random.nextInt()))
            .toString()
    }

    private fun getAddress(): String? {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        var results:String?= null
        return try {
            val addressList = geoCoder.getFromLocation(order.lat, order.lng, 1)
            results = if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))
                sb.toString()
            } else
                "Address not found**"
            results
        }catch (e:IOException){
            e.message
        }
    }

    // need to make sure that we have at least one item in cart before we move on to the status page
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.payment_address_button -> navController.navigate(R.id.action_foodOrderConfirmationFragment_to_paymentMethodFragment)
        }
    }
}