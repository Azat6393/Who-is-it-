package com.woynex.kimbu.feature_search.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.woynex.kimbu.R
import com.woynex.kimbu.databinding.FragmentSearchBinding
import com.woynex.kimbu.feature_search.presentation.adapter.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var _binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        _binding.viewPager.adapter = ViewPagerAdapter(this)
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
    }
}