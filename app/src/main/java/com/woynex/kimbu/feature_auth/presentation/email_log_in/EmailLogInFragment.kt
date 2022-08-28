package com.woynex.kimbu.feature_auth.presentation.email_log_in

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentEmailLogInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailLogInFragment : Fragment(R.layout.fragment_email_log_in) {

    private lateinit var _binding: FragmentEmailLogInBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailLogInBinding.bind(view)

        _binding.apply {
            signUpBtn.setOnClickListener {
                val action =
                    EmailLogInFragmentDirections.actionFragmentEmailLogInToFragmentEmailSignUp()
                findNavController().navigate(action)
            }
        }
    }
}