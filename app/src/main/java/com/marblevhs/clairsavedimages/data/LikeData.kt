package com.marblevhs.clairsavedimages.data

import com.google.gson.annotations.SerializedName


data class UnhandledLikeResponse(
    @SerializedName("response")
    val likeResponse: LikeResponse
)

data class LikeResponse(
    @SerializedName("liked")
    val liked: Int,
    @SerializedName("copied")
    val copied: Int
)



