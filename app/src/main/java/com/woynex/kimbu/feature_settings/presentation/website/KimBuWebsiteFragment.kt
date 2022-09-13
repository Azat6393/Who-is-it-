package com.woynex.kimbu.feature_settings.presentation.website

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentKimbuWebsiteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KimBuWebsiteFragment : Fragment(R.layout.fragment_kimbu_website) {

    private lateinit var _binding: FragmentKimbuWebsiteBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentKimbuWebsiteBinding.bind(view)

        _binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}