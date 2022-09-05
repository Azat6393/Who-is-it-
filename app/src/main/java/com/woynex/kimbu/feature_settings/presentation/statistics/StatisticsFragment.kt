package com.woynex.kimbu.feature_settings.presentation.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.databinding.FragmentStatisticsBinding
import com.woynex.kimbu.feature_search.domain.model.Statistics
import com.woynex.kimbu.feature_settings.presentation.adapter.SearchedDateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries


@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics),
    SearchedDateAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()
    private val mAdapter: SearchedDateAdapter by lazy { SearchedDateAdapter(this) }

    private var mRewardedAd: RewardedAd? = null
    private final var TAG = "StatisticsFragment"
    private var searchedUserName = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatisticsBinding.bind(view)

        _binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        initAdMob()
        initRecyclerView()
        observe()
    }

    private fun initAdMob() {
        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    p0.toString().let { Log.d(TAG, it) }
                    mRewardedAd = null
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    Log.d(TAG, "Ad was loaded.")
                    mRewardedAd = p0
                    mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.d(TAG, "Ad was clicked.")
                            showSearchedUserName()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            showSearchedUserName()
                            mRewardedAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            Log.e(TAG, "Ad failed to show fullscreen content.")
                            showSearchedUserName()
                            mRewardedAd = null
                        }

                        override fun onAdImpression() {
                            Log.d(TAG, "Ad recorded an impression.")
                            showSearchedUserName()

                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    }
                }
            })
    }

    private fun showFullScreenAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(requireActivity(), OnUserEarnedRewardListener() {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    var rewardAmount = rewardItem.amount
                    var rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward.")
                }
            })
        } else {
            showSearchedUserName()
        }
    }

    private fun showSearchedUserName() {
        if (searchedUserName.isNotBlank()) {
            Snackbar.make(requireView(), searchedUserName, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initRecyclerView() {
        _binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.statisticsResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> isLoading(false)
                        is Resource.Error -> {
                            isLoading(false)
                            if (result.data?.searched_id_list.isNullOrEmpty()) {
                                _binding.titleTv.text =
                                    "${getString(R.string.total_count)} 0"
                                drawChart(Statistics(searched_id_list = arrayListOf()))
                            }
                        }
                        is Resource.Loading -> isLoading(true)
                        is Resource.Success -> {
                            isLoading(false)
                            result.data?.let { drawChart(it) }
                            _binding.titleTv.text =
                                "${getString(R.string.total_count)} ${result.data?.searched_id_list?.size}"
                            mAdapter.submitList(result.data?.searched_id_list)
                            if (result.data?.searched_id_list.isNullOrEmpty()) {
                                _binding.titleTv.text =
                                    "${getString(R.string.total_count)} 0"
                                drawChart(Statistics(searched_id_list = arrayListOf()))
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userResponse.collect { result ->
                    when (result) {
                        is Resource.Empty -> isLoading(false)
                        is Resource.Error -> {
                            isLoading(false)
                            requireContext().showToastMessage(
                                result.message ?: getString(R.string.somethine_went_wrong)
                            )
                        }
                        is Resource.Loading -> isLoading(true)
                        is Resource.Success -> {
                            isLoading(false)
                            result.data?.let {
                                searchedUserName = "${it.first_name} ${it.last_name}"
                                showFullScreenAd()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun drawChart(statistics: Statistics) {
        val searchedList = mutableMapOf<String, Int>()

        statistics.searched_id_list?.forEach {
            val date = it.date?.millisToDate(Constants.chartDateFormat)
            if (searchedList.containsKey(date)) {
                searchedList[date!!] = searchedList[date]!!.plus(1)
            } else {
                searchedList[date!!] = 1
            }
        }

        val series = ValueLineSeries()
        series.color = 0xFF56B7F1.toInt()

        series.addPoint(ValueLinePoint(0f))

        searchedList.forEach { (s, i) ->
            println("$s -> $i")
            series.addPoint(ValueLinePoint(s, i.toFloat()))
        }
        series.addPoint(ValueLinePoint(searchedList.getLastValue().toFloat()))
        _binding.cubiclinechart.addSeries(series)
        _binding.cubiclinechart.startAnimation()
    }

    private fun isLoading(state: Boolean) {
        _binding.apply {
            progressBar.isVisible = state
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .visibility = View.VISIBLE
    }

    override fun onClick(id: String) {
        viewModel.getUser(id)
    }
}