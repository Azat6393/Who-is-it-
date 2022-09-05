package com.woynex.kimbu.feature_auth.presentation.email_log_in

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentEmailLogInBinding
import com.woynex.kimbu.feature_auth.presentation.AuthViewModel
import com.woynex.kimbu.feature_auth.presentation.sign_up.AuthFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.log

@AndroidEntryPoint
class EmailLogInFragment : Fragment(R.layout.fragment_email_log_in) {

    private lateinit var _binding: FragmentEmailLogInBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmailLogInBinding.bind(view)

        _binding.apply {
            signUpBtn.setOnClickListener {
                val action =
                    EmailLogInFragmentDirections.actionFragmentEmailLogInToFragmentEmailSignUp()
                findNavController().navigate(action)
            }
            loginBtn.setOnClickListener {
                logIn()
            }
        }
        observe()
    }

    private fun logIn() {
        if (isAllInputsFilled()) {
            viewModel.signInWithEmail(
                email = _binding.emailEt.text.toString(),
                password = _binding.passwordEt.text.toString(),
            )
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInResponse.collect {
                    when (it) {
                        is Resource.Empty -> isLoading(false)
                        is Resource.Error -> {
                            isLoading(false)
                            it.message?.let { it1 -> requireContext().showToastMessage(it1) }
                        }
                        is Resource.Loading -> isLoading(true)
                        is Resource.Success -> {
                            if (it.data?.phone_number?.isNotBlank() == true
                            ) {
                                val intent = Intent(requireActivity(), MainActivity::class.java)
                                startActivity(intent)
                                isLoading(false)
                                requireActivity().finish()
                            } else {
                                requireContext().showToastMessage("Success: ${it.data?.phone_number}")
                                it.data?.id?.let { id ->
                                    val action =
                                        AuthFragmentDirections.actionFragmentAuthToFragmentVerifyNumber(
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
    }

    private fun isLoading(state: Boolean) {
        _binding.apply {
            progressBar.isVisible = state
            loginBtn.isVisible = !state
            signUpBtn.isVisible = !state
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
            else -> {
                true
            }
        }
    }
}