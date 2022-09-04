package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class UpdateCallNumberUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    suspend operator fun invoke(numberInfo: NumberInfo) {
        repo.updateCallNumber(callNumber = numberInfo)
    }
}