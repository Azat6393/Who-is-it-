package com.woynex.kimbu.feature_search.domain.use_case

import com.woynex.kimbu.feature_search.domain.model.Contact
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    suspend operator fun invoke(): List<Contact> {
        return repo.getAllContacts()
    }
}