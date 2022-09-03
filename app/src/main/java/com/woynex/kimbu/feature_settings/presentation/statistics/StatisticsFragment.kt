package com.woynex.kimbu.feature_settings.presentation.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.*
import com.woynex.kimbu.databinding.FragmentStatisticsBinding
import com.woynex.kimbu.feature_search.domain.model.SearchedUser
import com.woynex.kimbu.feature_search.domain.model.Statistics
import com.woynex.kimbu.feature_settings.presentation.adapter.SearchedDateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries


@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var _binding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()
    private val mAdapter: SearchedDateAdapter by lazy { SearchedDateAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatisticsBinding.bind(view)

        _binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        initRecyclerView()
        observe()
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
}