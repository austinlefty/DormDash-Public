package com.dormdash.android.driver.bottomsheetbehavior

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentDriverHomeBottomSheetBinding
import com.dormdash.android.driver.viewmodel.DriverViewModel
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*


class DriverHomeBottomSheetFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var fragmentDriverHomeBottomSheetFragmentBinding : FragmentDriverHomeBottomSheetBinding? = null
    private val driverViewModel : DriverViewModel by activityViewModels()
    private val viewModel : CurrentUserSharedViewModel by activityViewModels()
    private lateinit var activeDataBase: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDriverHomeBottomSheetBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentDriverHomeBottomSheetFragmentBinding = binding
        // Inflate the layout for this fragment
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearUi()
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(false)
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryDistanceText.text = "${driverViewModel.totalDistance} mi"
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryDurationText.text = "${driverViewModel.totalDuration} mins"
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryEarningsText.text = "$7.56"
//
//        fragmentDriverHomeBottomSheetFragmentBinding.deliveryDurationText.text = duration.toString()
//        fragmentDriverHomeBottomSheetFragmentBinding.deliveryEarningsText.text = payment
        fragmentDriverHomeBottomSheetFragmentBinding!!.acceptWorkBtn.setOnClickListener(this)
        fragmentDriverHomeBottomSheetFragmentBinding!!.acceptWorkBtn.setOnClickListener(this)
    }


    private fun clearUi() {
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryDistanceText.text = ""
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryDurationText.text = ""
        fragmentDriverHomeBottomSheetFragmentBinding!!.deliveryEarningsText.text = ""
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.e(ContentValues.TAG, "*******************On Dismisssss Dialog*************")
        val dialog1 = getDialog()
        dialog1!!.dismiss()
    }

    private fun updateFireBaseDriverInfo() {
        val updateData = HashMap<String, Any>()
        updateData["status"] = driverViewModel.status
        updateData["driverFirstName"] = viewModel.currentUser!!.firstName.toString()
        updateData["driverLastName"] = viewModel.currentUser!!.lastName.toString()
        updateData["driverPhoneNumber"] = viewModel.currentUser!!.phoneNum!!
        Log.e(ContentValues.TAG, "******************* Driver Status = 11:*************: ${driverViewModel.status}")
        FirebaseDatabase.getInstance()
            .getReference("Drivers/Assigned/${viewModel.currentUser!!.uid!!}")
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener {
                Log.e(ContentValues.TAG, "******************* PASSED = 11:*************: $it")
            }
    }

    private fun addDriverToOrder() {
        val updateData = HashMap<String, Any>()
        updateData["driverID"] = viewModel.currentUser!!.uid.toString()
        val rID = driverViewModel.currentTask?.restaurantID
        val oID = driverViewModel.currentTask?.orderID
        val path = "Restaurants/$rID/Orders/$oID"
        FirebaseDatabase.getInstance().getReference(path)
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context, " "+ e.message, Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener {
                Log.e(ContentValues.TAG, "******************* addDriverToOrder PATH: $path *************: $it")
            }
    }


    private fun removeFromActive() {
        activeDataBase = FirebaseDatabase.getInstance().getReference("Drivers/Active/${viewModel.currentUser!!.uid!!}")
        activeDataBase.addListenerForSingleValueEvent(object : ValueEventListener {
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


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.accept_work_btn -> {
                Log.e(ContentValues.TAG, "*******************accept_work_btn25*************")
                driverViewModel.status = 2
//                removeFromActive()
                updateFireBaseDriverInfo()
                addDriverToOrder()

                findNavController().navigate(R.id.action_driverHomePageFragment_to_directionsFragment)
                dismiss()
            }
            R.id.decline_work_btn -> {
                Log.e(ContentValues.TAG, "*******************decline_work_btn26 *************")
                dismiss()
                //dialog!!.dismiss()
                // send the the task back to the restaurant to figure out how to assign new driver
                // also send notification to customer
                // remove task from driver
                // listen for new
            }
        }
    }



}