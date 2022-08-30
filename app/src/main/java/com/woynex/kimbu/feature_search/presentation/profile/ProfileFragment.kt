package com.woynex.kimbu.feature_search.presentation.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.requestPermission
import com.woynex.kimbu.core.utils.showAlertDialog
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var _binding: FragmentProfileBinding
    private val args: ProfileFragmentArgs by navArgs()
    private val viewModel: ProfileViewModel by viewModels()
    private var isBlocked = false

    private val requestCallPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCall()
            }
        }

    private val requestAddContactPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                addContact()
            }
        }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                requireContext().showToastMessage(getString(R.string.contact_added))
            }
            if (result.resultCode == Activity.RESULT_CANCELED) {
                requireContext().showToastMessage(getString(R.string.add_contact_cancelled))
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        _binding.apply {
            nameTv.text = if (args.numberInfo.name.isNullOrBlank()) args.numberInfo.number
            else args.numberInfo.name
            phoneNumberTv.text =
                "${args.numberInfo.number} - ${args.numberInfo.countryCode}"

            callButton.setOnClickListener {
                requestCallPermission()
            }
            addButton.setOnClickListener {
                requestAddContactPermission()
            }
            reportButton.setOnClickListener {
                if (isBlocked) {
                    requireContext().showAlertDialog(
                        getString(R.string.unblock_number_message),
                        getString(R.string.unblock_number)
                    ) {
                        unblockNumber()
                    }
                } else {
                    requireContext().showAlertDialog(
                        getString(R.string.block_number_message),
                        getString(R.string.block_number)
                    ) {
                        blockNumber()
                    }
                }
            }
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        viewModel.checkForBlockedNumber(args.numberInfo.number)
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBlocked.collect { result ->
                    isBlocked = result
                    if (result) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            _binding.reportButton.setColorFilter(requireContext().getColor(R.color.red))
                        } else {
                            _binding.reportButton.setColorFilter(
                                requireContext().resources.getColor(
                                    R.color.red
                                )
                            )
                        }
                        _binding.reportText.text = getString(R.string.unblock_number)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            _binding.reportButton.setColorFilter(requireContext().getColor(R.color.black))
                        } else {
                            _binding.reportButton.setColorFilter(
                                requireContext().resources.getColor(
                                    R.color.black
                                )
                            )
                        }
                        _binding.reportText.text = getString(R.string.block)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun blockNumber() {
        viewModel.blockNumber(number = args.numberInfo.number)
        viewModel.checkForBlockedNumber(args.numberInfo.number)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unblockNumber() {
        viewModel.unblockNumber(number = args.numberInfo.number)
        viewModel.checkForBlockedNumber(args.numberInfo.number)
    }

    private fun startCall() {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:${args.numberInfo.number}")
        requireActivity().startActivity(callIntent)
    }

    private fun addContact() {
        val addContactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
        addContactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
        addContactIntent
            .putExtra(ContactsContract.Intents.Insert.NAME, args.numberInfo.name)
            .putExtra(ContactsContract.Intents.Insert.PHONE, args.numberInfo.number)
        resultLauncher.launch(addContactIntent)
    }

    private fun requestAddContactPermission() {
        requestPermission(
            requireActivity(),
            requireView(),
            Manifest.permission.WRITE_CONTACTS,
            getString(R.string.add_contact_permission)
        ) { granted ->
            if (granted) {
                addContact()
            } else {
                requestAddContactPermissionLauncher.launch(
                    Manifest.permission.WRITE_CONTACTS
                )
            }
        }
    }

    private fun requestCallPermission() {
        requestPermission(
            requireActivity(),
            requireView(),
            Manifest.permission.CALL_PHONE,
            getString(R.string.call_permession_text)
        ) { granted ->
            if (granted) {
                startCall()
            } else {
                requestCallPermissionLauncher.launch(
                    Manifest.permission.CALL_PHONE
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .visibility = View.VISIBLE
    }
}