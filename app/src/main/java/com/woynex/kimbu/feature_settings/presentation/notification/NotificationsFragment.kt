package com.woynex.kimbu.feature_settings.presentation.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.woynex.kimbu.R
import com.woynex.kimbu.core.domain.model.NotificationModel
import com.woynex.kimbu.databinding.FragmentNotificationsBinding
import com.woynex.kimbu.feature_settings.presentation.adapter.NotificationAdapter
import com.woynex.kimbu.feature_settings.presentation.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications),
    NotificationAdapter.OnItemClickListener {

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var _binding: FragmentNotificationsBinding
    private val mAdapter: NotificationAdapter by lazy { NotificationAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationsBinding.bind(view)

        _binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        viewModel.getAllNotification()
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

    fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notifications.collect {
                    mAdapter.submitList(it)
                }
            }
        }
    }

    override fun delete(item: NotificationModel) {
        viewModel.deleteNotification(item)
    }

    override fun onClick(item: NotificationModel) {
        viewModel.updateNotification(item.copy(is_viewed = true))
        val action =
            NotificationsFragmentDirections.actionNotificationsFragmentToNotificationDetailsFragment(
                item
            )
        findNavController().navigate(action)
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