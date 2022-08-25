package com.woynex.kimbu.feature_search.presentation.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Constants.dateFormat
import com.woynex.kimbu.core.utils.millisToDate
import com.woynex.kimbu.databinding.FragmentFeedBinding
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.presentation.SearchFragment
import com.woynex.kimbu.feature_search.presentation.SearchFragmentDirections
import com.woynex.kimbu.feature_search.presentation.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var _binding: FragmentFeedBinding
    private val viewModel: SearchViewModel by activityViewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCallLog()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        _binding.showAllBtn.setOnClickListener {
            requireParentFragment().viewPager.currentItem = 2
        }
        requestPermission()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastCallLogs.collect { results ->
                    results.forEachIndexed { index, numberInfo ->
                        when (index) {
                            0 -> {
                                _binding.firstName.text =
                                    if (numberInfo.name.isNullOrBlank()) numberInfo.number
                                    else numberInfo.name
                                _binding.firstNumber.text = numberInfo.number
                                _binding.firstDate.text = numberInfo.date.millisToDate(
                                    dateFormat
                                )
                                _binding.firstCallLog.setOnClickListener {
                                    (requireParentFragment() as SearchFragment).removeGlobalLayoutListener()
                                    navigateToProfileScreen(results[0])
                                }
                            }
                            1 -> {
                                _binding.secondName.text =
                                    if (numberInfo.name.isNullOrBlank()) numberInfo.number
                                    else numberInfo.name
                                _binding.secondNumber.text = numberInfo.number
                                _binding.secondDate.text = numberInfo.date.millisToDate(
                                    dateFormat
                                )
                                _binding.secondCallLog.setOnClickListener {
                                    (requireParentFragment() as SearchFragment).removeGlobalLayoutListener()
                                    navigateToProfileScreen(results[1])
                                }
                            }
                            2 -> {

                                _binding.thirdName.text =
                                    if (numberInfo.name.isNullOrBlank()) numberInfo.number
                                    else numberInfo.name
                                _binding.thirdNumber.text = numberInfo.number
                                _binding.thirdDate.text = numberInfo.date.millisToDate(
                                    dateFormat
                                )
                                _binding.thirdCallLog.setOnClickListener {
                                    (requireParentFragment() as SearchFragment).removeGlobalLayoutListener()
                                    navigateToProfileScreen(numberInfo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToProfileScreen(numberInfo: NumberInfo) {
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment(numberInfo)
        findNavController().navigate(action)
    }

    private fun getCallLog() {
        viewModel.getLastCallLogs()
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
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