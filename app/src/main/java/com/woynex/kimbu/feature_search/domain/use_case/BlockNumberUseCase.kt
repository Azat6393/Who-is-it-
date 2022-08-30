package com.woynex.kimbu.feature_search.domain.use_case

import android.os.Build
import androidx.annotation.RequiresApi
import com.woynex.kimbu.feature_search.domain.repository.BlockedNumberRepository
import javax.inject.Inject

class BlockNumberUseCase @Inject constructor(
    private val repo: BlockedNumberRepository
) {
    @RequiresApi(Build.VERSION_CODES.N)
    suspend operator fun invoke(number: String) {
        repo.blockNumber(number)
    }
}