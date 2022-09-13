package com.woynex.kimbu.feature_settings.presentation.privacy_policy

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentPrivacyPolicyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private lateinit var _binding: FragmentPrivacyPolicyBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrivacyPolicyBinding.bind(view)

        _binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}