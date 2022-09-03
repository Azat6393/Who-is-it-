package com.woynex.kimbu.feature_settings.presentation.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentNotificationDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationDetailsFragment : Fragment(R.layout.fragment_notification_details) {

    private lateinit var _binding: FragmentNotificationDetailsBinding
    private val args: NotificationDetailsFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationDetailsBinding.bind(view)

        _binding.apply {
            titleTv.text = args.notification.title
            textTv.text = args.notification.text
            backBtn.setOnClickListener { findNavController().popBackStack() }
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