package com.marblevhs.clairsavedimages.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.marblevhs.clairsavedimages.data.UnhandledImageResponse
import javax.inject.Inject

class ImageApiPagingSource @Inject constructor(): PagingSource<Int, UnhandledImageResponse>() {
    override fun getRefreshKey(state: PagingState<Int, UnhandledImageResponse>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnhandledImageResponse> {
        TODO("Not yet implemented")
    }
}