package com.woynex.kimbu.feature_auth.presentation.email_log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.BottomSheetForgotPasswordBinding

class ForgotPasswordBottomSheet : BottomSheetDialogFragment() {

    private lateinit var _binding: BottomSheetForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetForgotPasswordBinding.bind(view)

        _binding.forgotBackButton.setOnClickListener {
            this.dismiss()
        }
        _binding.forgotSendButton.setOnClickListener {
            if (_binding.forgotEditText.text.toString().isBlank()) {
                requireContext().showToastMessage(getString(R.string.input_email_address))
            } else {
                isLoading(true)
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(_binding.forgotEditText.text.toString())
                    .addOnSuccessListener {
                        isLoading(false)
                        requireContext().showToastMessage(getString(R.string.please_check_your_mail))
                        this.dismiss()
                    }
                    .addOnFailureListener {
                        isLoading(false)
                        requireContext().showToastMessage(it.localizedMessage)
                    }
            }
        }
    }

    private fun isLoading(state: Boolean) {
        _binding.apply {
            forgotProgressBar.isVisible = state
            forgotSendButton.isVisible = !state
        }
    }
}