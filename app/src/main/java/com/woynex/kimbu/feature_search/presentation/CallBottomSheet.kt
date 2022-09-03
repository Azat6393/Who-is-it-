package com.woynex.kimbu.feature_search.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.BottomSheetCallBinding

class CallBottomSheet(private val call: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var _binding: BottomSheetCallBinding
    private var callNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetCallBinding.bind(view)

        initButtons()
    }

    private fun initButtons() {
        _binding.apply {
            deleteBtn.setOnClickListener {
                callNumber = callNumber.dropLast(1)
                updateCallNumber()
            }
            oneBtn.setOnClickListener {
                callNumber += "1"
                updateCallNumber()
            }
            twoBtn.setOnClickListener {
                callNumber += "2"
                updateCallNumber()
            }
            threeBtn.setOnClickListener {
                callNumber += "3"
                updateCallNumber()
            }
            fourBtn.setOnClickListener {
                callNumber += "4"
                updateCallNumber()
            }
            fiveBtn.setOnClickListener {
                callNumber += "5"
                updateCallNumber()
            }
            sixBtn.setOnClickListener {
                callNumber += "6"
                updateCallNumber()
            }
            sevenBtn.setOnClickListener {
                callNumber += "7"
                updateCallNumber()
            }
            eightBtn.setOnClickListener {
                callNumber += "8"
                updateCallNumber()
            }
            nineBtn.setOnClickListener {
                callNumber += "9"
                updateCallNumber()
            }
            zeroBtn.setOnClickListener {
                callNumber += "0"
                updateCallNumber()
            }
            zeroBtn.setOnLongClickListener {
                callNumber += "+"
                updateCallNumber()
                true
            }
            starBtn.setOnClickListener {
                callNumber += "*"
                updateCallNumber()
            }
            hashBtn.setOnClickListener {
                callNumber += "#"
                updateCallNumber()
            }
            callBtn.setOnClickListener {
                if (callNumber.isNotBlank()) {
                    call(callNumber)
                    this@CallBottomSheet.dismiss()
                }
            }
        }
    }

    private fun updateCallNumber() {
        _binding.numberTv.text = callNumber
    }
}