package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class SearchContactByNumberUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    operator fun invoke(number: String): String {
        return repo.searchContactByNumber(number)
    }
}