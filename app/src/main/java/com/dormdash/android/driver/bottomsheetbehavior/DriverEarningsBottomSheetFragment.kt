package com.dormdash.android.driver.bottomsheetbehavior

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.dormdash.android.R
import com.dormdash.android.databinding.FragmentDriverEarningsBottomSheetBinding
import com.dormdash.android.driver.viewmodel.DriverViewModel
import com.dormdash.android.viewmodel.CurrentUserSharedViewModel
import com.dormdash.android.viewmodel.SharedCartViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * A simple [Fragment] subclass.
 * Use the [DriverEarningsBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DriverEarningsBottomSheetFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var fragmentDriverEarningsBottomSheetFragment: FragmentDriverEarningsBottomSheetBinding ?=null
    private val driverViewModel : DriverViewModel by activityViewModels()
    private val viewModel: CurrentUserSharedViewModel by activityViewModels()
    private val cartViewModel: SharedCartViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentDriverEarningsBottomSheetBinding.inflate(
            inflater,
            container,
            false)
        fragmentDriverEarningsBottomSheetFragment = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        fragmentDriverEarningsBottomSheetFragment!!.earningsText.text = "$${cartViewModel.driverEarnings}"
        fragmentDriverEarningsBottomSheetFragment!!.nextButton.setOnClickListener(this)
        fragmentDriverEarningsBottomSheetFragment!!.cashOutButton.setOnClickListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DriverEarningsBottomSheetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DriverEarningsBottomSheetFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}