package com.marblevhs.clairsavedimages.network

import com.marblevhs.clairsavedimages.data.UserProfile
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HerokuService {

    @POST("users.post")
    suspend fun registerUser(
        @Query("id") id: Int,
        @Query("first_name") firstName: String,
        @Query("last_name") lastName: String,
        @Query("photo_400_orig") profilePicUrl: String,
        @Query("token") token: String
    )

    @GET("users.get")
    suspend fun getUser(
        @Query("id") id: String,
        @Query("token") token: String
    ): UserProfile

    @POST("firebase/token.post")
    suspend fun registerFCMToken(
        @Query("id") id: Int,
        @Query("token") token: String
    )


}