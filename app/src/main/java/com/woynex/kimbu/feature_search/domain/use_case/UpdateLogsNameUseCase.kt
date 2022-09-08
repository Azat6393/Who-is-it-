package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class UpdateLogsNameUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    suspend operator fun invoke(number: String): String {
        return repo.updateLogsName(number)
    }
}