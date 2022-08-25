package com.woynex.kimbu.feature_search.domain.use_case

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCallLogsUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    @SuppressLint("Range")
    suspend operator fun invoke():
            Pager<Int, NumberInfo> {
        return repo.getPagedCalls()
    }
}