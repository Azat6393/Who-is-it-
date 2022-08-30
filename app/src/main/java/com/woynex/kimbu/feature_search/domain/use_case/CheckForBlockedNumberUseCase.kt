package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CheckForBlockedNumberUseCase @Inject constructor(
    private val repo: BlockedNumberRepository
) {
    suspend operator fun invoke(number: String): Flow<Boolean> = flow {
        val blockedList = repo.getBlockedNumbers()
        blockedList.forEach { blockedNumber ->
            if (blockedNumber == number) {
                emit(true)
                return@forEach
            }
        }
    }
}