package com.woynex.kimbu.feature_auth.presentation.verify_number

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentVerifyNumberBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyNumberFragment : Fragment(R.layout.fragment_verify_number) {

    private lateinit var _binding: FragmentVerifyNumberBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyNumberBinding.bind(view)

        _binding.continueBtn.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

    }
}