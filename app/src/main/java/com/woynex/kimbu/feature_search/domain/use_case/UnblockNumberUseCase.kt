package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.repository.BlockedNumberRepository
import javax.inject.Inject

class UnblockNumberUseCase @Inject constructor(
    private val repo: BlockedNumberRepository
) {
    suspend operator fun invoke(number: String) {
        return repo.unblockNumber(number)
    }
}