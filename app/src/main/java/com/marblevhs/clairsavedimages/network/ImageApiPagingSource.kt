package com.marblevhs.clairsavedimages.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.data.toLocalImage
import com.marblevhs.clairsavedimages.notSecrets.NotSecrets
import retrofit2.HttpException


class ImageApiPagingSource(
    private val imageService: ImageService,
    private val accessToken: String,
    private val query: String
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
            var ownerId = ""
            var albumId = ""
            var rev = 1
            when (query) {
                "saved_ascendant" -> {
                    ownerId = NotSecrets.CLAIR_ID
                    albumId = "saved"
                    rev = 0
                }
                "saved_descendant" -> {
                    ownerId = NotSecrets.CLAIR_ID
                    albumId = "saved"
                    rev = 1
                }
                "public_ascendant" -> {
                    ownerId = NotSecrets.PUBLIC_ID
                    albumId = "wall"
                    rev = 0
                }
                "public_descendant" -> {
                    ownerId = NotSecrets.PUBLIC_ID
                    albumId = "wall"
                    rev = 1
                }
                "-1" -> {

                }
                else -> {
                    throw IllegalArgumentException("Illegal query argument")
                }
            }
            val images: List<LocalImage> = imageService.requestImages(
                ownerId = ownerId,
                albumId = albumId,
                rev = rev,
                count = pageSize,
                offset = pageSize * (pageNumber - 1),
                accessToken = accessToken
            ).imageResponse.images.map {
                it.toLocalImage(ownerId = ownerId, album = albumId)
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