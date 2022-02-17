package com.marblevhs.clairsavedimages.imageRepo


import com.marblevhs.clairsavedimages.BuildConfig
import com.marblevhs.clairsavedimages.secrets.Secrets
import com.marblevhs.clairsavedimages.data.JsonImage
import com.marblevhs.clairsavedimages.network.ImageApi
import javax.inject.Inject

interface Repo{
    suspend fun getImages(ownerId: String, albumId: String, rev: Int): List<JsonImage>
    suspend fun getIsLiked(itemId: String, ownerId: String): Boolean
    suspend fun addLike(itemId: String, ownerId: String)
    suspend fun deleteLike(itemId: String, ownerId: String)

}

class RepoImpl @Inject constructor(private val api: ImageApi): Repo {


    override suspend fun getImages(ownerId: String, albumId: String, rev: Int): List<JsonImage> {
        val images: MutableList<JsonImage> = mutableListOf()
        for(i in 0..BuildConfig.PAGES_QUANTITY){
            val response = api.requestImages(
                ownerId = ownerId,
                albumId = albumId,
                rev = rev,
                count = BuildConfig.PAGE_SIZE,
                offset = i * BuildConfig.PAGE_SIZE,
                accessToken = Secrets.ACCESS_TOKEN)
            images.addAll(response.imageResponse.images)
        }
        return images
    }

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        val isLiked = api.requestIsLiked(
            ownerId = ownerId,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        ).likeResponse.liked
        return (isLiked == 1)
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

}