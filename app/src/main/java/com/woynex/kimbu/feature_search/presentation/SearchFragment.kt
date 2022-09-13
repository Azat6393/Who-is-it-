package com.woynex.kimbu.feature_search.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.databinding.FragmentSearchBinding
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.presentation.adapter.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var _binding: FragmentSearchBinding
    private var selectedCountry: CountryInfo? = null
    private val viewModel: SearchViewModel by viewModels()

    private var mInterstitialAd: InterstitialAd? = null
    private var searchedNumber: NumberInfo? = null
    private final var TAG = "SearchFragment"

    private var searchedNumberString = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)


        _binding.profileLogo.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment()
            findNavController().navigate(action)
        }

        initAdMob()
        initFab()
        initViewPager()
        initSearchCountryEditText()
        initAutoComplete()
        observe()
    }

    private fun initAdMob() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-8594335878312175/2469418225",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    p0.toString().let { Log.d(TAG, it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = p0
                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                Log.d(TAG, "Ad was clicked.")
                                searchedNumber?.let { navigateToProfileScreen(it) }
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad dismissed fullscreen content.")
                                searchedNumber?.let { navigateToProfileScreen(it) }
                                mInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                Log.e(TAG, "Ad failed to show fullscreen content.")
                                searchedNumber?.let { navigateToProfileScreen(it) }
                                mInterstitialAd = null
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Ad recorded an impression.")
                                searchedNumber?.let { navigateToProfileScreen(it) }
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.")
                            }
                        }
                }
            })
    }

    private fun showFullScreenAd(number: NumberInfo) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(requireActivity())
        } else {
            navigateToProfileScreen(number)
        }
    }

    private fun navigateToProfileScreen(number: NumberInfo) {
        _binding.progressBar.visibility = View.GONE
        val action =
            SearchFragmentDirections.actionSearchFragmentToProfileFragment(
                number
            )
        findNavController().navigate(action)
    }

    private fun initFab() {
        _binding.callFab.setOnClickListener {
            CallBottomSheet() { number ->
                if (requireContext().isAppDefaultDialer()) {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:$number")
                    requireActivity().startActivity(callIntent)
                } else {
                    requireContext().showToastMessage(getString(R.string.set_kim_bu_as_default))
                }
            }.show(childFragmentManager, "Call Bottom Sheet")
        }
    }


    private fun initSearchCountryEditText() {
        _binding.searchButton.setOnClickListener {
            if (requireContext().isAppDefaultDialer()) {
                selectedCountry?.let { country ->
                    val number = _binding.searchEditText.text.toString()
                    if (number.isBlank()) {
                        Toast.makeText(requireContext(), "Please input number", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        val phoneNumber = "${country.number}$number"
                        searchedNumberString = phoneNumber
                        viewModel.searchPhoneNumber(phoneNumber)
                        hideKeyboard()
                    }
                }
            } else {
                requireContext().showToastMessage(getString(R.string.set_kim_bu_as_default))
            }
        }
    }

    private fun initViewPager() {
        _binding.viewPager.adapter = ViewPagerAdapter(this)
        _binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(_binding.tabLayout, _binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.feed)
                }
                1 -> {
                    tab.text = getString(R.string.call_history)
                }
            }
        }.attach()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.phoneNumberResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> {
                            _binding.progressBar.visibility = View.GONE
                        }
                        is Resource.Error -> {
                            viewModel.clearPhoneNumberResponse()
                            _binding.progressBar.visibility = View.GONE
                            searchedNumber = NumberInfo(
                                id = 0,
                                name = "",
                                number = searchedNumberString,
                                type = "",
                                countryCode = "",
                                date = 0,
                                profilePhoto = ""
                            )
                            showFullScreenAd(searchedNumber!!)
                            viewModel.clearPhoneNumberResponse()
                        }
                        is Resource.Loading -> {
                            _binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            val numberInfo = result.data
                            numberInfo?.let {
                                removeGlobalLayoutListener()
                                searchedNumber = it
                                showFullScreenAd(it)
                                _binding.searchEditText.setText("")
                                viewModel.clearPhoneNumberResponse()
                            }
                        }
                    }
                }
            }
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
                    hideKeyboard()
                    CountriesDialog(countryList) {
                        showKeyboard()
                        fillCountryInfo(it)
                    }.show(childFragmentManager, "Country Dialog")
                }
            }
        }
    }

    private fun hideKeyboard() {
        val view: View = requireView()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
            countryCode.text = countryInfo.name
            countryCodeNumber.text = countryInfo.number
            selectedCountry = countryInfo
        }
    }

    fun removeGlobalLayoutListener() {
        requireView().viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val r = Rect()
        requireView().getWindowVisibleDisplayFrame(r);
        val screenHeight = requireView().rootView.height;

        val keypadHeight = screenHeight - r.bottom

        if (keypadHeight > screenHeight * 0.15) {
            searchContentVisibility(true)
        } else {
            searchContentVisibility(false)
        }
    }

    private fun setKeyboardVisibilityListener() {
        requireView().viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun searchContentVisibility(visible: Boolean) {
        requireActivity()
            .findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
            .isVisible = !visible
        _binding.searchContainer.isVisible = visible
        _binding.callFab.isVisible = !visible
    }

    override fun onStop() {
        super.onStop()
        removeGlobalLayoutListener()
    }

    override fun onStart() {
        super.onStart()
        setKeyboardVisibilityListener()
    }
}