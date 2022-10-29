package com.woynex.kimbu.feature_auth.presentation.commitment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.showToastMessage
import com.woynex.kimbu.databinding.FragmentCommitmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommitmentFragment : Fragment(R.layout.fragment_commitment) {

    private lateinit var _binding: FragmentCommitmentBinding

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommitmentBinding.bind(view)

        val mSharedPreferences =
            requireActivity().getSharedPreferences("privacy_policy", Context.MODE_PRIVATE)
        val isAccepted = mSharedPreferences.getBoolean("is_accepted", false);
        if (isAccepted){
            val action =
                CommitmentFragmentDirections.actionCommitmentFragmentToFragmentAuth()
            findNavController().navigate(action)
        }

        _binding.apply {
            privacyPolicyBtn.setOnClickListener {

            }
            termsOfServiceBtn.setOnClickListener {

            }
            continueBtn.setOnClickListener {
                if (_binding.agreeCheckBox.isChecked) {
                    val editor = mSharedPreferences.edit()
                    editor.putBoolean("is_accepted", true)
                    editor.apply()
                    val action =
                        CommitmentFragmentDirections.actionCommitmentFragmentToFragmentAuth()
                    findNavController().navigate(action)
                } else {
                    requireContext().showToastMessage(getString(R.string.please_accept_permission))
                }
            }
        }
    }
}