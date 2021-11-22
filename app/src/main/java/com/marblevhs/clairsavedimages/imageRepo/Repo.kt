package com.marblevhs.clairsavedimages.imageRepo

import android.util.Log
import com.marblevhs.clairsavedimages.BuildConfig
import com.marblevhs.clairsavedimages.secrets.Secrets
import com.marblevhs.clairsavedimages.data.Image
import com.marblevhs.clairsavedimages.network.ApiProvider


class Repo {

    private val api = ApiProvider().getApi()
    suspend fun getImages(): List<Image> {
        val images: MutableList<Image> = mutableListOf()
        for(i in 0..BuildConfig.PAGES_QUANTITY){
            val response = api.requestImages(
                ownerId = Secrets.OWNER_ID,
                albumId = "saved",
                count = BuildConfig.PAGE_SIZE,
                offset = i * BuildConfig.PAGE_SIZE,
                accessToken = Secrets.ACCESS_TOKEN)
            images.addAll(response.imageResponse.images)
        }
        return images
    }

    suspend fun getIsLiked(itemId: String): Boolean {
        val isLiked = api.requestIsLiked(
            ownerId = Secrets.OWNER_ID,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        ).likeResponse.liked
        return (isLiked == 1)
    }
    suspend fun addLike(itemId: String){
        api.requestAddLike(
            ownerId = Secrets.OWNER_ID,
            accessToken = Secrets.ACCESS_TOKEN,
            itemId = itemId
        )
    }

}