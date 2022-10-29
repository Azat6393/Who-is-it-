package com.woynex.kimbu.feature_search.presentation

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.databinding.FragmentSearchBinding
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.presentation.adapter.ViewPagerAdapter
import com.woynex.kimbu.feature_settings.presentation.web_view.PopUpDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var _binding: FragmentSearchBinding
    private var selectedCountry: CountryInfo? = null
    private val viewModel: SearchViewModel by viewModels()

    private var searchedNumber: NumberInfo? = null
    private final var TAG = "SearchFragment"

    private var searchedNumberString = ""
    var isSearchFragment = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        _binding.profileLogo.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment()
            findNavController().navigate(action)
        }
        initViewPager()
        initSearchCountryEditText()
        initAutoComplete()
        observe()

        if (!requireContext().isAppDefaultDialer()) {
            _binding.callFab.isVisible = false
        }


        _binding.searchEditText.setOnFocusChangeListener { view, b ->
            if (b) {
                isSearchFragment = 1
            }
        }
    }

    private fun showFullScreenAd(number: NumberInfo) {
        PopUpDialog(
            onClick = {
                val action = SearchFragmentDirections.actionSearchFragmentToWebViewFragment()
                findNavController().navigate(action)
            },
            onClose = {
                navigateToProfileScreen(number)
            }
        ).show(childFragmentManager, "PopUp Dialog")
    }

    private fun navigateToProfileScreen(number: NumberInfo) {
        _binding.progressBar.visibility = View.GONE
        val action =
            SearchFragmentDirections.actionSearchFragmentToProfileFragment(
                number
            )
        findNavController().navigate(action)
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
        _binding.viewPager.offscreenPageLimit = 2
        TabLayoutMediator(_binding.tabLayout, _binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.feed)
                }
                1 -> {
                    tab.text = getString(R.string.call_history)
                }
                2 -> {
                    tab.text = getString(R.string.contacts)
                }
            }
        }.attach()
        _binding.viewPager.setCurrentItem(0, false)
        _binding.viewPager.registerOnPageChangeCallback(onPageChange)

    }

    private val onPageChange = object : OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            if (position == 0 || position == 1) {
                showBarsVisibility(true)
            }
        }
    }

    fun showBarsVisibility(state: Boolean) {
        val dip = -100f
        val px = applyDimension(
            COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val params: LayoutParams = _binding.topViews.layoutParams as LayoutParams
                if (state) {
                    params.topMargin = (0 * interpolatedTime).toInt()
                } else {
                    params.topMargin = (px * interpolatedTime).toInt()
                }
                _binding.topViews.layoutParams = params
            }
        }
        a.duration = 500
        _binding.topViews.startAnimation(a)

        if (requireContext().isAppDefaultDialer()) {
            if (state) {
                _binding.callFab.show()
            } else {
                _binding.callFab.hide()
            }
        }
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
        if (isSearchFragment == 1){
            _binding.searchContainer.isVisible = visible
        }
        if (isSearchFragment == 2){
            _binding.searchContainer.isVisible = false
        }
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