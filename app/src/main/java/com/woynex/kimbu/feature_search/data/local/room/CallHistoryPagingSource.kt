package com.woynex.kimbu.feature_search.data.local.room


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.woynex.kimbu.feature_search.domain.model.NumberInfo
import kotlinx.coroutines.delay

class CallHistoryPagingSource(
    private val dao: CallHistoryDao
) : PagingSource<Int, NumberInfo>() {

    override fun getRefreshKey(state: PagingState<Int, NumberInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NumberInfo> {
        val page = params.key ?: 0

        return try {
            val entities = dao.getPagedList(params.loadSize, page * params.loadSize)
            if (page != 0) delay(1000)
            LoadResult.Page(
                data = entities,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (entities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}