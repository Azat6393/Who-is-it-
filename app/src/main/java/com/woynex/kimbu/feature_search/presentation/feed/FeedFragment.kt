package com.woynex.kimbu.feature_search.presentation.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.woynex.kimbu.R
import com.woynex.kimbu.core.utils.Constants.dateFormat
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.core.utils.millisToDate
import com.woynex.kimbu.databinding.FragmentFeedBinding
import com.woynex.kimbu.feature_search.data.model.NumberInfo
import com.woynex.kimbu.feature_search.presentation.SearchFragmentDirections
import com.woynex.kimbu.feature_search.presentation.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var _binding: FragmentFeedBinding
    private val viewModel: SearchViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        _binding.showAllBtn.setOnClickListener {
            requireParentFragment().viewPager.currentItem = 2
        }
        viewModel.getCallLog()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callLogs.collect { results ->
                    when (results) {
                        is Resource.Empty -> Unit
                        is Resource.Error -> Unit
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            results.data?.forEachIndexed { index, numberInfo ->
                                when (index) {
                                    0 -> {
                                        _binding.firstName.text = numberInfo.name
                                        _binding.firstNumber.text = numberInfo.number
                                        _binding.firstDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                    1 -> {
                                        _binding.secondName.text = numberInfo.name
                                        _binding.secondNumber.text = numberInfo.number
                                        _binding.secondDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                    2 -> {
                                        _binding.thirdName.text = numberInfo.name
                                        _binding.thirdNumber.text = numberInfo.number
                                        _binding.thirdDate.text = numberInfo.date.millisToDate(
                                            dateFormat
                                        )
                                    }
                                }
                            }
                            _binding.firstCallLog.setOnClickListener {
                                results.data?.get(0)?.let { numberInfo ->
                                    navigateToProfileScreen(numberInfo)
                                }
                            }
                            _binding.secondCallLog.setOnClickListener {
                                results.data?.get(1)?.let { numberInfo ->
                                    navigateToProfileScreen(numberInfo)
                                }
                            }
                            _binding.thirdCallLog.setOnClickListener {
                                results.data?.get(2)?.let { numberInfo ->
                                    navigateToProfileScreen(numberInfo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToProfileScreen(numberInfo: NumberInfo) {
        val action = SearchFragmentDirections.actionSearchFragmentToProfileFragment(numberInfo)
        findNavController().navigate(action)
    }
}