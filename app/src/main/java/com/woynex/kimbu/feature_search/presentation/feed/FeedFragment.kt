package com.woynex.kimbu.feature_search.presentation.feed

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.core.Constants.dateFormat
import com.woynex.kimbu.core.Resource
import com.woynex.kimbu.core.millisToDate
import com.woynex.kimbu.databinding.FragmentFeedBinding
import com.woynex.kimbu.feature_search.presentation.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var _binding: FragmentFeedBinding
    private val viewModel: FeedViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCallLog()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)
        requestPermission()

        _binding.showAllBtn.setOnClickListener {
            requireParentFragment().viewPager.currentItem = 2
        }
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callLogs.collect { results ->
                    when (results) {
                        is Resource.Empty -> Unit
                        is Resource.Error -> Unit
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            results.data?.forEachIndexed { index, numberInfo ->
                                when (index) {
                                    0 -> {
                                        _binding.firstName.text = numberInfo.name
                                        _binding.firstNumber.text = numberInfo.number
                                        _binding.firstDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                    1 -> {
                                        _binding.secondName.text = numberInfo.name
                                        _binding.secondNumber.text = numberInfo.number
                                        _binding.secondDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                    2 -> {
                                        _binding.thirdName.text = numberInfo.name
                                        _binding.thirdNumber.text = numberInfo.number
                                        _binding.thirdDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCallLog() {
        viewModel.getCallLog()
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                getCallLog()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_CALL_LOG
            ) -> {
                // Additional rationale should be displayed
                Snackbar.make(
                    requireActivity().findViewById(R.id.container),
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.ok)) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_CALL_LOG
                    )
                }.show()
            }
            else -> {
                // Permission has not been asked yet
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALL_LOG
                )
            }
        }
    }
}