package com.woynex.kimbu.feature_search.domain.use_case

import android.util.Log
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class GetLastCallLogsUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    operator fun invoke(): Flow<List<NumberInfo>> = flow {
        try {
            emit(repo.getLastCallLogs())
        } catch (e: Exception) {
            Log.d("GetLastCallLogs:", e.localizedMessage ?: "GetLastCallLogs error")
        }
    }
}