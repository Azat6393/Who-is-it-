package com.woynex.kimbu.feature_search.domain.use_case

import coil.network.HttpException
import com.woynex.kimbu.core.utils.Resource
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import com.woynex.kimbu.feature_search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import javax.inject.Inject

class SearchPhoneNumberUseCase @Inject constructor(
    private val repo: SearchRepository
) {
    operator fun invoke(phoneNumber: String): Flow<Resource<NumberInfo>> = flow {
        try {
            emit(Resource.Loading<NumberInfo>())
            val response = repo.searchPhoneNumber(phoneNumber)
            emit(Resource.Success<NumberInfo>(response))
        } catch (e: HttpException) {
            emit(Resource.Error<NumberInfo>(e.localizedMessage ?: "Something went wrong"))
        } catch (e: IOException) {
            emit(Resource.Error<NumberInfo>(e.localizedMessage ?: "Something went wrong"))
        }
    }
}