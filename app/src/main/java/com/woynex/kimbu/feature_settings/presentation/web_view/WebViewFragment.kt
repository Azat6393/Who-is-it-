package com.woynex.kimbu.feature_settings.presentation.web_view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentPrivacyPolicyBinding
import com.woynex.kimbu.databinding.FragmentWebViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewFragment : Fragment(R.layout.fragment_web_view) {

    private lateinit var _binding: FragmentWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWebViewBinding.bind(view)

        _binding.webView.settings.apply {
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            databaseEnabled = true
            domStorageEnabled = true
            javaScriptEnabled = true
            displayZoomControls = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false

            allowContentAccess = true
            allowFileAccess = true

            setSupportMultipleWindows(false)
        }
        _binding.webView.loadUrl("https://www.kargobul.com.tr/")
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