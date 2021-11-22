package com.marblevhs.clairsavedimages.network

import com.marblevhs.clairsavedimages.data.UnhandledImageResponse
import com.marblevhs.clairsavedimages.data.LikeResponse
import com.marblevhs.clairsavedimages.data.UnhandledLikeResponse
import org.json.JSONObject
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

class ApiProvider {
    fun getApi(): Api {
        return RetrofitProvider().getRetrofit().create()
    }

    companion object{
        fun newInstance() = ApiProvider()
    }
}


interface Api {
    @GET("method/photos.get")
    suspend fun requestImages(
        @Query("owner_id") ownerId: String,
        @Query("album_id") albumId: String,
        @Query("photo_sizes") photoSizes: Int = 1,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
        @Query("rev") rev: Int = 1,
        @Query("access_token") accessToken: String,
        @Query("v") v: String = "5.81"
    ): UnhandledImageResponse

    @GET("method/likes.isLiked")
    suspend fun requestIsLiked(
        @Query("owner_id") ownerId: String,
        @Query("type") type: String = "photo",
        @Query("item_id") itemId: String,
        @Query("access_token") accessToken: String,
        @Query("v") v: String = "5.81"
    ): UnhandledLikeResponse

    @GET("method/likes.add")
    suspend fun requestAddLike(
        @Query("owner_id") ownerId: String,
        @Query("type") type: String = "photo",
        @Query("item_id") itemId: String,
        @Query("access_token") accessToken: String,
        @Query("v") v: String = "5.81"
    )

}