package com.marblevhs.clairsavedimages.data


import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

data class UnhandledImageResponse(
    @SerializedName("response")
    val imageResponse: ImageResponse
)

data class ImageResponse(
    @SerializedName("items")
    val images: List<JsonImage>
)


data class JsonImage(
    @SerializedName("id")
    val id: String,
    @SerializedName("sizes")
    val sizes: List<Size>
)


data class Size(
    @SerializedName("type")
    val type: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("url")
    val imageUrl: String
)

@Entity(
    tableName = "LocalImage",
    primaryKeys = ["id", "ownerId", "album"]
)
data class LocalImage(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "ownerId")
    val ownerId: String,
    @ColumnInfo(name = "album")
    val album: String,
    @ColumnInfo(name = "width")
    val width: Int,
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "thumbnailUrl")
    val thumbnailUrl: String,
    @ColumnInfo(name = "fullSizeUrl")
    val fullSizeUrl: String
)
