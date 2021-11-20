package com.marblevhs.clairsavedimages.network

import com.marblevhs.clairsavedimages.data.Response
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
        @Query("rev") rev: Int,
        @Query("access_token") accessToken: String,
        @Query("v") v: String = "5.81"
    ): Response
}