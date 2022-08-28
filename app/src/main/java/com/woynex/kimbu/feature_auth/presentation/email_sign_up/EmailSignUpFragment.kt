package com.woynex.kimbu.feature_auth.presentation.email_sign_up

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentEmailSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailSignUpFragment : Fragment(R.layout.fragment_email_sign_up) {

    private lateinit var _binding: FragmentEmailSignUpBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailSignUpBinding.bind(view)

        _binding.apply {

            continueBtn.setOnClickListener {
                val action =
                    EmailSignUpFragmentDirections.actionFragmentEmailSignUpToFragmentVerifyNumber()
                findNavController().navigate(action)
            }
        }
    }
}