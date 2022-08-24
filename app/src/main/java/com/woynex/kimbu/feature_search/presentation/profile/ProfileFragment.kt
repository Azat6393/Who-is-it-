package com.woynex.kimbu.feature_search.presentation.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var _binding: FragmentProfileBinding
    private val args: ProfileFragmentArgs by navArgs()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCall()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        _binding.apply {
            nameTv.text = args.numberInfo.name
            phoneNumberTv.text =
                "${args.numberInfo.number} - ${args.numberInfo.countryCode}"

            callButton.setOnClickListener {
                requestPermission()
            }
            addButton.setOnClickListener {

            }
            reportButton.setOnClickListener {

            }
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun startCall() {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:${args.numberInfo.number}")
        requireActivity().startActivity(callIntent)
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                startCall()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) -> {
                // Additional rationale should be displayed
                Snackbar.make(
                    requireActivity().findViewById(R.id.container),
                    getString(R.string.call_permession_text),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.ok)) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CALL_PHONE
                    )
                }.show()
            }
            else -> {
                // Permission has not been asked yet
                requestPermissionLauncher.launch(
                    Manifest.permission.CALL_PHONE
                )
            }
        }
    }
}