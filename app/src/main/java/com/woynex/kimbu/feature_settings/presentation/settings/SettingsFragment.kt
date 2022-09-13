package com.woynex.kimbu.feature_settings.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.decode.SvgDecoder
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.woynex.kimbu.AuthActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.showAlertDialog
import com.woynex.kimbu.databinding.FragmentSettingsBinding
import com.woynex.kimbu.feature_auth.domain.model.toNumberInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var _binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        _binding.apply {
            themeSwitch.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            signOutBtn.setOnClickListener {
                requireContext().showAlertDialog(
                    getString(R.string.sign_out_message),
                    getString(R.string.sign_out)
                ) {
                    viewModel.signOut()
                    startActivity(Intent(requireActivity(), AuthActivity::class.java))
                    requireActivity().finish()
                }
            }
            statisticsItem.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToStatisticsFragment()
                findNavController().navigate(action)
            }
            notificationsBtn.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToNotificationsFragment()
                findNavController().navigate(action)
            }
            nameTv.setOnClickListener {
                viewModel.user.let {
                    val action =
                        SettingsFragmentDirections.actionSettingsFragmentToUserProfileFragment()
                    findNavController().navigate(action)
                }
            }
            aboutUsBtn.setOnClickListener {
                val action = SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment()
                findNavController().navigate(action)
            }
            kimbuWebsiteBtn.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToKimBuWebsiteFragment()
                findNavController().navigate(action)
            }
            privacyPolicyBtn.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToPrivacyPolicyFragment()
                findNavController().navigate(action)
            }
        }
        viewModel.getUnwatchedNotifications()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    _binding.nameTv.text = "${user.first_name} ${user.last_name}"
                    _binding.profilePhotoIv.load(user.profile_photo) {
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.unwatchedNotifications.collect { size ->
                    _binding.notificationCount.text = size.toString()
                    _binding.notificationCountCardView.isVisible = size > 0
                }
            }
        }
    }
}