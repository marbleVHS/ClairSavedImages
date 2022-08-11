package com.marblevhs.clairsavedimages.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnhandledLikeResponse(
    @Json(name = "response")
    val likeResponse: LikeResponse
)

@JsonClass(generateAdapter = true)
data class LikeResponse(
    @Json(name = "liked")
    val liked: Int,
    @Json(name = "copied")
    val copied: Int
)



