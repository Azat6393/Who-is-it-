package com.woynex.kimbu.feature_search.presentation

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.decode.SvgDecoder
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Constants
import com.woynex.kimbu.core.utils.fromJsonToCountyList
import com.woynex.kimbu.core.utils.getJsonFromAssets
import com.woynex.kimbu.databinding.FragmentSearchBinding
import com.woynex.kimbu.feature_search.domain.model.CountryInfo
import com.woynex.kimbu.feature_search.presentation.adapter.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var _binding: FragmentSearchBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        _binding.viewPager.adapter = ViewPagerAdapter(this)
        _binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(_binding.tabLayout, _binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Feed"
                }
                1 -> {
                    tab.text = "Call History"
                }
            }
        }.attach()
        setKeyboardVisibilityListener()
        initAutoComplete()
    }

    private fun initAutoComplete() {
        val countryList = getJsonFromAssets(requireContext(), Constants.countryListJsonName)
            ?.fromJsonToCountyList()
        val currentCountry = countryList?.find { it.name == "Turkey" }
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
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .isVisible = !visible
        _binding.searchContainer.isVisible = visible
    }
}