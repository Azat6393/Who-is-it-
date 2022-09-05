package com.woynex.kimbu.feature_auth.presentation.email_sign_up

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentEmailSignUpBinding
import com.woynex.kimbu.feature_auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmailSignUpFragment : Fragment(R.layout.fragment_email_sign_up) {

    private lateinit var _binding: FragmentEmailSignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailSignUpBinding.bind(view)

        _binding.apply {
            continueBtn.setOnClickListener {
                createAccount()
            }
        }
        observe()
    }

    private fun createAccount() {
        if (isAllInputsFilled()) {
            viewModel.signUpWithEmail(
                email = _binding.emailEt.text.toString(),
                password = _binding.passwordEt.text.toString(),
                lastName = _binding.lastNameEt.text.toString(),
                firstName = _binding.firstNameEt.text.toString()
            )
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpResponse.collect {
                    when (it) {
                        is Resource.Empty -> isLoading(false)
                        is Resource.Error -> {
                            isLoading(false)
                            it.message?.let { it1 -> requireContext().showToastMessage(it1) }
                        }
                        is Resource.Loading -> isLoading(true)
                        is Resource.Success -> {
                            isLoading(false)
                            it.data?.id?.let { id ->
                                viewModel.clearSignUpResponse()
                                val action =
                                    EmailSignUpFragmentDirections.actionFragmentEmailSignUpToFragmentVerifyNumber(
                                        id
                                    )
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isLoading(state: Boolean) {
        _binding.apply {
            progressBar.isVisible = state
            continueBtn.isVisible = !state
        }
    }

    private fun isAllInputsFilled(): Boolean {
        return when {
            _binding.emailEt.text.toString().isBlank() -> {
                requireContext().showToastMessage(getString(R.string.input_email_address))
                return false
            }
            _binding.passwordEt.text.toString().isBlank() -> {
                requireContext().showToastMessage(getString(R.string.input_password))
                return false
            }
            _binding.firstNameEt.text.toString().isBlank() -> {
                requireContext().showToastMessage(getString(R.string.input_first_name))
                return false
            }
            _binding.lastNameEt.text.toString().isBlank() -> {
                requireContext().showToastMessage(getString(R.string.input_last_name))
                return false
            }
            else -> {
                true
            }
        }
    }
}