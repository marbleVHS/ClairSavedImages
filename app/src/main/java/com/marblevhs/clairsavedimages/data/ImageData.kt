package com.marblevhs.clairsavedimages.data

import com.google.gson.annotations.SerializedName

data class UnhandledImageResponse(
    @SerializedName("response")
    val imageResponse: ImageResponse
)

data class ImageResponse(
    @SerializedName("items")
    val images: List<Image>
)

data class Image(
    @SerializedName("id")
    val id: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("sizes")
    val sizes: List<Size>
)

data class Size(
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val imageUrl: String
)