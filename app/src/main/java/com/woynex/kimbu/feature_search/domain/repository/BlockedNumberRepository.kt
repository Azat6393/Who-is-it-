package com.woynex.kimbu.feature_search.domain.repository

interface BlockedNumberRepository {

    suspend fun blockNumber(number: String)

    suspend fun unblockNumber(number: String)

    suspend fun getBlockedNumbers(): List<String>
}

