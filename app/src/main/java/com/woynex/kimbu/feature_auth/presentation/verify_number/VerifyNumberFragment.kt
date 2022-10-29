package com.woynex.kimbu.feature_auth.presentation.verify_number

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import com.woynex.kimbu.MainActivity
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.databinding.FragmentVerifyNumberBinding
import com.woynex.kimbu.feature_auth.presentation.AuthViewModel
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import com.woynex.kimbu.feature_search.presentation.CountriesDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyNumberFragment : Fragment(R.layout.fragment_verify_number) {

    private lateinit var _binding: FragmentVerifyNumberBinding
    private val viewModel: AuthViewModel by viewModels()
    private var selectedCountry: CountryInfo? = null
    private val args: VerifyNumberFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyNumberBinding.bind(view)

        _binding.closeButton.setOnClickListener {
            val action = VerifyNumberFragmentDirections.actionFragmentVerifyNumberToFragmentAuth()
            findNavController().navigate(action)
        }
        _binding.continueBtn.setOnClickListener {
            if (isInputFilled()) {
                viewModel.updatePhoneNumber(
                    "${selectedCountry?.number}${_binding.phoneNumberTv.text}",
                    args.userId
                )
            }
        }
        initAutoComplete()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.phoneNumberResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> isLoading(false)
                        is Resource.Error -> {
                            isLoading(false)
                            result.message?.let { requireContext().showToastMessage(it) }
                        }
                        is Resource.Loading -> isLoading(true)
                        is Resource.Success -> {
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                            isLoading(false)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }

    private fun isLoading(state: Boolean) {
        _binding.apply {
            progressBar.isVisible = state
            continueBtn.isVisible = !state
        }
    }

    private fun initAutoComplete() {
        val countryList = getJsonFromAssets(requireContext(), Constants.countryListJsonName)
            ?.fromJsonToCountyList()
        val currentCountry = countryList?.find { it.name == "Turkey" }
        selectedCountry = currentCountry
        currentCountry?.let { fillCountryInfo(it) }
        _binding.apply {
            countryInfoContainer.setOnClickListener {
                if (countryList != null) {
                    CountriesDialog(countryList) {
                        fillCountryInfo(it)
                    }.show(childFragmentManager, "Country Dialog")
                }
            }
        }
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val inflater = super.onGetLayoutInflater(savedInstanceState)
        val mSharedPreferences = requireActivity().getSharedPreferences("UI", Context.MODE_PRIVATE)
        val isDarkMode = mSharedPreferences.getBoolean("DARK_MODE", false)
        return if (isDarkMode) {
            val contextThemeWrapper: Context = ContextThemeWrapper(requireContext(), R.style.Theme_KimBu_Dark)
            inflater.cloneInContext(contextThemeWrapper)
        } else {
            val contextThemeWrapper: Context = ContextThemeWrapper(requireContext(), R.style.Theme_KimBu_Light)
            inflater.cloneInContext(contextThemeWrapper)
        }
    }

    private fun fillCountryInfo(countryInfo: CountryInfo) {
        _binding.apply {
            countryFlag.load(countryInfo.flag) {
                crossfade(true)
                placeholder(R.drawable.ic_baseline_image_24)
                decoderFactory(SvgDecoder.Factory())
                transformations(CircleCropTransformation())
                build()
            }
            countryCodeNumber.text = countryInfo.number
            selectedCountry = countryInfo
        }
    }

    private fun isInputFilled(): Boolean {
        return when {
            _binding.phoneNumberTv.text.toString().isBlank() -> {
                requireContext().showToastMessage(getString(R.string.input_phone_number))
                false
            }
            else -> true
        }
    }
}