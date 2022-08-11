package com.dormdash.android.ui.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dormdash.android.R
import com.dormdash.android.database.FoodItem
import com.dormdash.android.databinding.FragmentPaymentMethodBinding
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.firebase.auth.FirebaseAuth


/**
 * A simple [Fragment] subclass.
 * Use the [PaymentMethodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentMethodFragment : Fragment(), View.OnClickListener, AdapterView.OnItemClickListener {

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cartList: ArrayList<FoodItem>
    private var fragmentPaymentMethodBinding: FragmentPaymentMethodBinding ?= null
    private val cartViewModel: SharedCartViewModel by activityViewModels()
    private val studentModel: CurrentUserSharedViewModel by activityViewModels()

    private lateinit var studentIDchecked: CheckBox
    private lateinit var creditCardChecked: Button
    private lateinit var ccNumber: TextView
    private lateinit var mmTextView: TextView
    private lateinit var yyTextView: TextView
    private lateinit var cvvTextView: TextView
    private lateinit var nameOnCard: TextView
    lateinit var saveBtn: Button
    lateinit var clearBtn: Button
    private lateinit var warningText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentPaymentMethodBinding.inflate(inflater, container, false)
        fragmentPaymentMethodBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //initializing all fields
        cartList = ArrayList()
        cartList = cartViewModel.getList() as ArrayList<FoodItem>

        studentIDchecked = fragmentPaymentMethodBinding!!.studentIDCheckBox
        creditCardChecked = fragmentPaymentMethodBinding!!.creditCardCheckBox

        ccNumber = fragmentPaymentMethodBinding!!.cardNumText
        mmTextView = fragmentPaymentMethodBinding!!.mmEditText
        yyTextView = fragmentPaymentMethodBinding!!.yyEditText
        cvvTextView = fragmentPaymentMethodBinding!!.codeEditTextNum
        nameOnCard = fragmentPaymentMethodBinding!!.nameOnCardText
        warningText = fragmentPaymentMethodBinding!!.warningText
        warningText.visibility = View.INVISIBLE

        fragmentPaymentMethodBinding!!.saveButton.setOnClickListener(this)
        fragmentPaymentMethodBinding!!.clearButton.setOnClickListener(this)
        fragmentPaymentMethodBinding!!.paymentCheckoutCancelButton.setOnClickListener(this)

        fillTextFields()
        setCheckedChangedListener()
       // onCheckBoxClicked()

    }

    private fun studentIdSelected() {
        cartViewModel.studentIDselected = true
    }

    private fun fillTextFields() {
         if (!(cartViewModel.creditCardNumber.isNullOrEmpty())) {
             ccNumber.text = cartViewModel.creditCardNumber.toString()
             mmTextView.text = cartViewModel.mmExpiration.toString()
             yyTextView.text = cartViewModel.yyExpiration.toString()
             cvvTextView.text = cartViewModel.cvvCode.toString()
             nameOnCard.text = cartViewModel.nameOnCard.toString()
         }
     }
    private fun setCheckedChangedListener() {
        fragmentPaymentMethodBinding!!.studentIDCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            studentIdSelected()
            warningText.visibility = View.VISIBLE
        }
        fragmentPaymentMethodBinding!!.creditCardCheckBox.setOnClickListener {
            fragmentPaymentMethodBinding!!.cardNumText.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.mmEditText.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.yyEditText.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.codeEditTextNum.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.nameOnCardText.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.saveButton.visibility = View.VISIBLE
            fragmentPaymentMethodBinding!!.clearButton.visibility = View.VISIBLE
        }
    }


//    private fun studentIdIsChecked() {
//        if(fragmentPaymentMethodBinding!!.studentIDCheckBox.isChecked) {
//            sendStudentIDtoFirebase()
//        }
//    }

//    private fun sendStudentIDtoFirebase() {
//        TODO("Not yet implemented")
//    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment paymentMethodFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PaymentMethodFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.payment_checkout_cancel_button -> navController.navigate(R.id.action_paymentMethodFragment_to_foodOrderConfirmationFragment)
            R.id.clearButton -> {
                fragmentPaymentMethodBinding!!.cardNumText.setText("")
                fragmentPaymentMethodBinding!!.mmEditText.setText("")
                fragmentPaymentMethodBinding!!.yyEditText.setText("")
                fragmentPaymentMethodBinding!!.codeEditTextNum.setText("")
                fragmentPaymentMethodBinding!!.nameOnCardText.setText("")
            }
            R.id.saveButton -> {
                if (ccNumber.text.toString().isEmpty() || ccNumber.text.toString().length != 16){
                    Toast.makeText(requireContext(),"Input correct Credit Card number",
                        Toast.LENGTH_SHORT).show()
                }
                else if(mmTextView.text.toString().isEmpty() || mmTextView.text.toString().length != 2){
                    Toast.makeText(requireContext(),"Input correct Expiration Month",
                        Toast.LENGTH_SHORT).show()
                }
                else if(yyTextView.text.toString().isEmpty() || yyTextView.text.toString().length != 2){
                    Toast.makeText(requireContext(),"Input correct Expiration Month",
                        Toast.LENGTH_SHORT).show()
                }
                else if(cvvTextView.text.toString().isEmpty() || cvvTextView.text.toString().length != 3){
                    Toast.makeText(requireContext(),"Input correct CVV number",
                        Toast.LENGTH_SHORT).show()
                }
                else if(nameOnCard.text.toString().isEmpty() || mmTextView.text.toString().length < 2){
                    Toast.makeText(requireContext(),"Input Name on Card correctly",
                        Toast.LENGTH_SHORT).show()
                }
                else {
                    //NEED TO IMPLEMENT ELSE
//                    cartViewModel.order!!.creditCardNumber = ccNumber.text.toString()
//                    cartViewModel.order!!.mmExpiration = mmTextView.text.toString()
//                    cartViewModel.order!!.yyExpiration = yyTextView.text.toString()
//                    cartViewModel.order!!.cvvCode = cvvTextView.text.toString()
//                    cartViewModel.order!!.nameOnCard = nameOnCard.text.toString()

                    cartViewModel.setCreditCardInfo(ccNumber.text.toString(),mmTextView.text.toString(),
                        yyTextView.text.toString(), cvvTextView.text.toString(), nameOnCard.text.toString())
                    cartViewModel.cardAdded = true
                }
            }
//            R.id.creditCard_checkBox -> {
//                fragmentPaymentMethodBinding!!.cardNumText.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.mmEditText.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.yyEditText.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.codeEditTextNum.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.nameOnCardText.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.saveButton.visibility = View.VISIBLE
//                fragmentPaymentMethodBinding!!.clearButton.visibility = View.VISIBLE
//            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, positon: Int, id: Long) {
        if (requireView().findViewById<Button>(R.id.payment_checkout_cancel_button).isEnabled) {
            if (view != null) {
                when (view.id) {
                    R.id.payment_checkout_cancel_button -> navController.navigate(R.id.action_paymentMethodFragment_to_foodOrderConfirmationFragment)
                }
            }
        }
    }
}