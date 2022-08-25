package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.core.utils.convertToSet
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class UpdateCallLogsUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    suspend operator fun invoke() {
        val cursorCalls: Set<NumberInfo> = convertToSet(repo.getCallLogs())
        val daoCalls: Set<NumberInfo> = convertToSet(repo.getCallsFromDao())

        cursorCalls.minus(daoCalls)
        repo.insertCall(cursorCalls.toList())
    }
}
