package com.woynex.kimbu.feature_auth.presentation.sign_up

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var _binding: FragmentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        _binding.apply {
            loginWithEmailBtn.setOnClickListener {
                val action = AuthFragmentDirections.actionFragmentAuthToFragmentEmailLogIn()
                findNavController().navigate(action)
            }
        }
    }

}