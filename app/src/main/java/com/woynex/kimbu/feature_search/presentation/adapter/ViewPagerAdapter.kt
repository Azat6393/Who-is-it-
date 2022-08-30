package com.woynex.kimbu.feature_search.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.woynex.kimbu.feature_search.presentation.call_history.CallHistoryFragment
import com.woynex.kimbu.feature_search.presentation.feed.FeedFragment


class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CallHistoryFragment()
            else -> FeedFragment()
        }
    }
}