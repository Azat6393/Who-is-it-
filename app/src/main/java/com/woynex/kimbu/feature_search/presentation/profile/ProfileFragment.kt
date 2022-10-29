package com.woynex.kimbu.feature_search.presentation.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BlockedNumberContract.canCurrentUserBlockNumbers
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.decode.SvgDecoder
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.requestPermission
import com.woynex.kimbu.core.utils.showAlertDialog
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentProfileBinding
import com.woynex.kimbu.feature_search.presentation.adapter.TagsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var _binding: FragmentProfileBinding
    private val args: ProfileFragmentArgs by navArgs()
    private val viewModel: ProfileViewModel by viewModels()
    private val mAdapter: TagsAdapter by lazy { TagsAdapter() }
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
                viewModel.updateLogsName()
                _binding.nameTv.text = viewModel.searchContactByNumber(args.numberInfo.number)
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
                "${args.numberInfo.number}  ${args.numberInfo.countryCode}"

            viewModel.searchNumber(args.numberInfo)

            callButton.setOnClickListener {
                requestCallPermission()
            }
            addButton.setOnClickListener {
                requestAddContactPermission()
            }
            reportButton.setOnClickListener {
                if (canCurrentUserBlockNumbers(requireContext())) {
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
                } else {
                    requireContext().showToastMessage("Cannot")
                }
            }
            backBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        initRecyclerView()
        viewModel.getTags(args.numberInfo.number)
        if (canCurrentUserBlockNumbers(requireContext())) {
            viewModel.checkForBlockedNumber(args.numberInfo.number)
        }
        observe()
    }


    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBlocked.collect { result ->
                    isBlocked = result
                    if (result) {
                        _binding.reportButton.setColorFilter(requireContext().getColor(R.color.red))
                        _binding.reportText.text = getString(R.string.unblock_number)
                    } else {
                        _binding.reportButton.setColorFilter(requireContext().getColor(R.color.black))
                        _binding.reportText.text = getString(R.string.block)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.numberResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> Unit
                        is Resource.Error -> Unit
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            _binding.nameTv.text = result.data?.name
                            if (!result.data?.profilePhoto.isNullOrBlank()) {
                                _binding.profilePhotoIv.load(result.data?.profilePhoto) {
                                    crossfade(false)
                                    placeholder(R.drawable.profile_photo)
                                    decoderFactory(SvgDecoder.Factory())
                                    transformations(CircleCropTransformation())
                                    scale(Scale.FILL)
                                    build()
                                }
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tagsResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> _binding.progressBar.isVisible = false

                        is Resource.Error -> _binding.progressBar.isVisible = false
                        is Resource.Loading -> _binding.progressBar.isVisible = true
                        is Resource.Success -> {
                            _binding.progressBar.isVisible = false
                            mAdapter.submitList(result.data)
                        }
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun blockNumber() {
        viewModel.blockNumber(number = args.numberInfo.number)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unblockNumber() {
        viewModel.unblockNumber(number = args.numberInfo.number)
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
            .findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
            .visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        requireActivity()
            .findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
            .visibility = View.VISIBLE
    }
}