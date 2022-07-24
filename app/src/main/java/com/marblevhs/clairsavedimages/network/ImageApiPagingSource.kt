package com.marblevhs.clairsavedimages.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.data.toLocalImage
import retrofit2.HttpException


class ImageApiPagingSource(
    private val imageService: ImageService,
    private val accessToken: String,
    private val album: Album,
    private val rev: Int
) : PagingSource<Int, LocalImage>() {

    override fun getRefreshKey(state: PagingState<Int, LocalImage>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LocalImage> {
        val pageNumber = params.key ?: 1
        val pageSize = params.loadSize.coerceAtMost(999)
        try {
            val images: List<LocalImage> = imageService.requestImages(
                ownerId = album.ownerId,
                albumId = album.albumId,
                rev = rev,
                count = pageSize,
                offset = pageSize * (pageNumber - 1),
                accessToken = accessToken
            ).imageResponse.images.map {
                it.toLocalImage(ownerId = album.ownerId, album = album.albumId)
            }
            val nextPageNumber = if (images.isEmpty()) null else pageNumber + 1
            val prevPageNumber = if (pageNumber > 1) pageNumber - 1 else null
            return LoadResult.Page(
                data = images,
                nextKey = nextPageNumber,
                prevKey = prevPageNumber
            )
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }


}