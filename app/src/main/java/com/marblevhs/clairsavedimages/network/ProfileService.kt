package com.marblevhs.clairsavedimages.network

import com.marblevhs.clairsavedimages.data.UserProfileResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileService {

    @GET("method/users.get")
    suspend fun requestProfileInfo(
        @Query("access_token") accessToken: String,
        @Query("fields") fields: String = "photo_400_orig",
        @Query("v") v: String = "5.81"
    ): UserProfileResponse


}