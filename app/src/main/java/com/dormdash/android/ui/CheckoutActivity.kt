package com.dormdash.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.braintreepayments.cardform.view.CvvEditText
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.database.Order
import com.dormdash.android.databinding.ActivityCheckoutBinding
import com.dormdash.android.ui.util.PaymentsUtil

import com.google.android.gms.wallet.*
import org.json.JSONException
import org.json.JSONObject

import com.dormdash.android.ui.viewmodel.CheckoutViewModel
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.android.gms.common.api.ApiException
import org.json.JSONArray


/**
 * Checkout implementation for the app
 */
class CheckoutActivity : AppCompatActivity(), View.OnClickListener {

    // Arbitrarily-picked constant integer you define to track a request for payment data activity.
    private val loadPaymentDataRequestCode = 991

    private lateinit var cartList:ArrayList<FoodItem>
    //private lateinit var order: Order
    private val model: CheckoutViewModel by viewModels()
    private val cartViewModel: SharedCartViewModel by viewModels()
    private val userViewModel: CurrentUserSharedViewModel by viewModels()

    private lateinit var layoutBinding: ActivityCheckoutBinding
    private lateinit var googlePayButton: View

    private lateinit var paymentsClient: PaymentsClient

    lateinit var navController: NavController



    private lateinit var garmentList: JSONArray
    private lateinit var selectedGarment: JSONObject



    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see AppCompatActivity.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use view binding to access the UI elements
        layoutBinding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        //cartViewModel = ViewModelProvider(this).get(SharedCartViewModel::class.java)

        googlePayButton = layoutBinding.googlePayButton.root
        googlePayButton.setOnClickListener { requestPayment() }
//        layoutBinding.paymentCheckoutCancelButton.setOnClickListener{
//            navController.navigate(R.id.foodOrderConfirmationFragment)}

        // Check whether Google Pay can be used to complete a payment
        model.canUseGooglePay.observe(this, Observer(::setGooglePayAvailable))


    }

    //NEED TO IMPLEMENT
    fun sendStudentIDtoFirebase() {

    }



    /**
     * Determine the viewer's ability to pay with a payment method supported by your app and display a
     * Google Pay payment button.
     *
     * @see [](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html.isReadyToPay
    ) */
    private fun possiblyShowGooglePayButton() {

        val isReadyToPayJson = PaymentsUtil.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                this,
                "Unfortunately, Google Pay is not available on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val dummyPriceCents = 100L
        val shippingCostCents = 900L
        val task = model.getLoadPaymentDataTask(dummyPriceCents + shippingCostCents)

        // Shows the payment sheet and forwards the result to the onActivityResult method.
        AutoResolveHelper.resolveTask(task, this, loadPaymentDataRequestCode)
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    @Suppress("Deprecation")
    // Suppressing deprecation until `registerForActivityResult` is available on the Google Pay API.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Value passed in AutoResolveHelper
            loadPaymentDataRequestCode -> {
                when (resultCode) {
                    RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }

                    RESULT_CANCELED -> {
                        // The user cancelled the payment attempt
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.isClickable = true
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            //TOAST HAS BEEN COMMENTED OUT
//            Toast.makeText(
//                this,
//                getString(R.string.payments_show_name, billingName),
//                Toast.LENGTH_LONG
//            ).show()

            // Logging token string.
            Log.d(
                "GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
            )

        } catch (error: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $error")
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", "Error code: $statusCode")
    }

    override fun onClick(v: View?) {
        when(v!!.id) {

            }

    }

}

//    fun onClick(v: View?) {
//        when (v!!.id) {
//            R.id.payment_checkout_cancel_button -> navController.navigate(R.id.foodOrderConfirmationFragment)
//        }
//    }


