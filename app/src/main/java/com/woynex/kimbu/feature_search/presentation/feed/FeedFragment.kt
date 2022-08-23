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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.core.Constants.dateFormat
import com.woynex.kimbu.core.millisToDate
import com.woynex.kimbu.databinding.FragmentFeedBinding
import com.woynex.kimbu.feature_search.presentation.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var _binding: FragmentFeedBinding

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
    }

    @SuppressLint("Range")
    private fun getCallLog() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                val uriCallLogs = Uri.parse("content://call_log/calls")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val cursorCallLogs =
                        requireActivity().contentResolver.query(
                            uriCallLogs, null, null, null
                        )
                    cursorCallLogs?.let {
                        cursorCallLogs.moveToLast()

                        for (i in 1..3) {
                            val stringNumber =
                                cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER))
                            val stringName =
                                cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            val stringDate =
                                cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.DATE))
                            when (i) {
                                1 -> {
                                    _binding.firstName.text =
                                        if (stringName.isBlank() || stringName == null) stringNumber
                                        else stringName
                                    _binding.firstNumber.text = stringNumber
                                    _binding.firstDate.text = stringDate.toLong().millisToDate(
                                        dateFormat
                                    )
                                }
                                2 -> {
                                    _binding.secondName.text =
                                        if (stringName.isBlank() || stringName == null) stringNumber
                                        else stringName
                                    _binding.secondNumber.text = stringNumber
                                    _binding.secondDate.text = stringDate.toLong().millisToDate(
                                        dateFormat
                                    )
                                }
                                3 -> {
                                    _binding.thirdName.text =
                                        if (stringName.isBlank() || stringName == null) stringNumber
                                        else stringName
                                    _binding.thirdNumber.text = stringNumber
                                    _binding.thirdDate.text = stringDate.toLong().millisToDate(
                                        dateFormat
                                    )
                                }
                            }
                            cursorCallLogs.moveToPrevious()
                        }
                        cursorCallLogs.close()
                    }
                }
            }
        }
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