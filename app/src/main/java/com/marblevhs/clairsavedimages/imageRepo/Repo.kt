package com.marblevhs.clairsavedimages.imageRepo


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.marblevhs.clairsavedimages.secrets.Secrets
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.network.ImageApi
import com.marblevhs.clairsavedimages.network.ImageApiPagingSource
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface Repo{
    suspend fun getImagesPaging(query: String): Flow<PagingData<LocalImage>>
    suspend fun getIsLiked(itemId: String, ownerId: String): Boolean
    suspend fun getIsFav(itemId: String, ownerId: String): Boolean
    suspend fun addLike(itemId: String, ownerId: String)
    suspend fun deleteLike(itemId: String, ownerId: String)
    suspend fun getFavs(rev: Int): List<LocalImage>
    suspend fun addFav(image: LocalImage)
    suspend fun deleteFav(image: LocalImage)

}

class RepoImpl @Inject constructor(private val api: ImageApi, private val db: DatabaseStorage): Repo {

    override suspend fun getImagesPaging(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 400,
                initialLoadSize = 400,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {ImageApiPagingSource(imageApi = api, query = query)}
        ).flow

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        val isLiked = api.requestIsLiked(
            ownerId = ownerId,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        ).likeResponse.liked
        return (isLiked == 1)
    }

    override suspend fun getIsFav(itemId: String, ownerId: String): Boolean {
        val isFav = db.imageDao().getImageByImageId(itemId)
        return isFav != null
    }

    override suspend fun addLike(itemId: String, ownerId: String){
        api.requestAddLike(
            ownerId = ownerId,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        )
    }
    override suspend fun deleteLike(itemId: String, ownerId: String){
        api.requestDeleteLike(
            ownerId = ownerId,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        )
    }

    override suspend fun getFavs(rev: Int): List<LocalImage> {
        var favs = db.imageDao().allImage
        if(rev == 1){
            favs = favs.asReversed()
        }
        return favs
    }

    override suspend fun addFav(image: LocalImage) {
        db.imageDao().insert(image = image)
    }

    override suspend fun deleteFav(image: LocalImage) {
        db.imageDao().delete(image = image)
    }

}