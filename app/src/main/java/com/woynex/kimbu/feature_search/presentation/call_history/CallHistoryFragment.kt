package com.woynex.kimbu.feature_search.presentation.call_history

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.isAppDefaultDialer
import com.woynex.kimbu.core.utils.requestPermission
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentCallHistoryBinding
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.presentation.SearchFragment
import com.woynex.kimbu.feature_search.presentation.SearchFragmentDirections
import com.woynex.kimbu.feature_search.presentation.SearchViewModel
import com.woynex.kimbu.feature_search.presentation.adapter.CallHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallHistoryFragment : Fragment(R.layout.fragment_call_history),
    CallHistoryAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentCallHistoryBinding
    private val mAdapter: CallHistoryAdapter by lazy { CallHistoryAdapter(this) }
    private val viewModel: SearchViewModel by activityViewModels()
    private var number = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCall(number)
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallHistoryBinding.bind(view)

        if (requireContext().isAppDefaultDialer()) {
            initContent()
        } else {
            _binding.setAsDefaultBtn.setOnClickListener {
                offerReplacingDefaultDialer()
            }
        }
    }

    private fun initContent() {
        _binding.setAsDefaultView.visibility = View.GONE
        viewModel.getCallLog()
        initRecyclerView()
        observe()
    }

    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callLogs.collect { result ->
                    result?.let {
                        mAdapter.submitData(it)
                    }
                }
            }
        }
    }

    override fun onClick(numberInfo: NumberInfo) {
        (requireParentFragment() as SearchFragment).removeGlobalLayoutListener()
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment(numberInfo)
        findNavController().navigate(action)
    }

    override fun onCallClick(numberInfo: NumberInfo) {
        (requireParentFragment() as SearchFragment).removeGlobalLayoutListener()
        number = numberInfo.number
        requestCallPhonePermission()
    }

    private fun startCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        requireActivity().startActivity(callIntent)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    requireContext().packageName
                )
            }
            resultLauncher.launch(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager?
            val intent = roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            resultLauncher.launch(intent)
        }
    }

    private fun requestCallPhonePermission() {
        requestPermission(
            requireActivity(),
            requireView(),
            Manifest.permission.CALL_PHONE,
            getString(R.string.permission_required)
        ) { granted ->
            if (granted) {
                startCall(number)
            } else {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALL_LOG
                )
            }
        }
    }
}