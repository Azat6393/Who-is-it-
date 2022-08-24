package com.woynex.kimbu.feature_search.presentation.call_history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.databinding.FragmentCallHistoryBinding
import com.woynex.kimbu.feature_search.data.model.NumberInfo
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCallHistoryBinding.bind(view)

        viewModel.getCallLog()
        initRecyclerView()
        observe()
    }

    private fun initRecyclerView() {
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callLogs.collect { result ->
                    when (result) {
                        is Resource.Empty -> Unit
                        is Resource.Error -> Unit
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            mAdapter.submitList(result.data)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(numberInfo: NumberInfo) {
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment(numberInfo)
        findNavController().navigate(action)
    }

    override fun onCallClick(numberInfo: NumberInfo) {
        number = numberInfo.number
        requestPermission()
    }

    private fun startCall(number: String) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number")
        requireActivity().startActivity(callIntent)
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted
                startCall(number)
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