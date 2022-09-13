package com.woynex.kimbu.feature_search.presentation.feed

import android.Manifest
import android.app.role.RoleManager
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.core.utils.Constants.dateFormat
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        if (requireContext().isAppDefaultDialer()) {
            initContent()
        } else {
            _binding.setAsDefaultBtn.setOnClickListener {
                if (_binding.chackBox.isChecked) {
                    viewModel.updateHasPermission(true)
                    offerReplacingDefaultDialer()
                } else {
                    requireContext().showToastMessage(getString(R.string.please_accept_permission))
                }
            }
        }
        initAdMob()
    }

    private fun initAdMob() {
        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()
        _binding.adView.loadAd(adRequest)
    }


    private fun initContent() {
        _binding.setAsDefaultView.visibility = View.GONE
        _binding.showAllBtn.setOnClickListener {
            requireParentFragment().viewPager.currentItem = 2
        }
        requestReadCallLogPermission()
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

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    viewModel.updateCallLogs()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    Log.d(
                        "Default Dialer request",
                        "User declined request to become default dialer"
                    )
                }
                else -> Log.d("Default Dialer request", "Unexpected result code $result.resultCode")
            }
        }


    private fun offerReplacingDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(ROLE_SERVICE) as RoleManager?
            val intent = roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            resultLauncher.launch(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    requireContext().packageName
                )
            }
            resultLauncher.launch(intent)
        }
    }

    private fun requestReadCallLogPermission() {
        requestPermission(
            requireActivity(),
            requireView(),
            Manifest.permission.READ_CALL_LOG,
            getString(R.string.permission_required)
        ) { granted ->
            if (granted) {
                getCallLog()
            } else {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALL_LOG
                )
            }
        }
    }
}