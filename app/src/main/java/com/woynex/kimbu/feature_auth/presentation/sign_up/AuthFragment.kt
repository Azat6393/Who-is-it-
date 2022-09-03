package com.woynex.kimbu.feature_auth.presentation.sign_up

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentAuthBinding
import com.woynex.kimbu.feature_auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private val TAG: String = "Google Sign in"
    private lateinit var _binding: FragmentAuthBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var singInRequest: BeginSignInRequest
    private val viewModel: AuthViewModel by viewModels()
    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true
    private lateinit var callBackManager: CallbackManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        callBackManager = CallbackManager.Factory.create()

        lifecycleScope.launch {
            if (viewModel.isAuth.value && viewModel.currentUser.first().phone_number.toString()
                    .isBlank()
            ) {
                viewModel.currentUser.first().id?.let {
                    val action =
                        AuthFragmentDirections.actionFragmentAuthToFragmentVerifyNumber(
                            it
                        )
                    findNavController().navigate(action)
                }
            }
        }

        _binding.apply {
            loginWithEmailBtn.setOnClickListener {
                val action = AuthFragmentDirections.actionFragmentAuthToFragmentEmailLogIn()
                findNavController().navigate(action)
            }
            loginWithGoogleBtn.setOnClickListener {
                initGoogleSignInRequest()
                beginSignIn()
            }
            loginWithFacebookBtn.setOnClickListener {

                LoginManager.getInstance().logInWithReadPermissions(
                    this@AuthFragment,
                    arrayListOf("email", "public_profile", "user_friends")
                )
                registerFacebookCallBack()
            }
        }
        observe()
    }

    private fun registerFacebookCallBack() {
        LoginManager.getInstance()
            .registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    requireContext().showToastMessage(getString(R.string.canceled))
                }

                override fun onError(error: FacebookException) {
                    requireContext().showToastMessage(
                        "Error is: ${error.localizedMessage}"
                    )
                }

                override fun onSuccess(result: LoginResult) {
                    viewModel.logInWithFacebook(result.accessToken)
                }
            })
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
            loginWithGoogleBtn.isVisible = !state
            loginWithFacebookBtn.isVisible = !state
            loginWithEmailBtn.isVisible = !state
        }
    }

    private fun beginSignIn() {
        oneTapClient.beginSignIn(singInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(
                        "Google Sign in",
                        "Couldn't start One Tap UI: ${e.localizedMessage}"
                    )
                    requireContext().showToastMessage("Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }.addOnFailureListener { e ->
                Log.d("Google Sign in", e.localizedMessage ?: "Error")
                requireContext().showToastMessage("Google Sign in Error is ${e.localizedMessage}")
            }
    }

    private fun initGoogleSignInRequest() {
        oneTapClient = Identity.getSignInClient(requireActivity())
        singInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("1005644502335-ssh1r0q0h0n0ip3q3jtp92ti4ahmo8k4.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callBackManager.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val firstName = credential.givenName
                    val lastName = credential.familyName
                    val password = credential.password

                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            viewModel.logInWithGoogle(idToken, firstName ?: "", lastName ?: "")
                            Log.d("Google Sign in", "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d("Google Sign in", "Got password.")
                        }
                        else -> {
                            Log.d("Google Sign in", "No ID token or password!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(
                                TAG, "Couldn't get credential from result." +
                                        " (${e.localizedMessage})"
                            )
                        }
                    }
                    requireContext().showToastMessage("Error is: ${e.localizedMessage}")
                }
            }
        }
    }
}