package com.marblevhs.clairsavedimages.data


import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class UnhandledImageResponse(
    @Json(name = "response")
    val imageResponse: ImageResponse
)

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "items")
    val images: List<JsonImage>
)

@JsonClass(generateAdapter = true)
data class JsonImage(
    @Json(name = "id")
    val id: String,
    @Json(name = "sizes")
    val sizes: List<Size>
)

@JsonClass(generateAdapter = true)
data class Size(
    @Json(name = "type")
    val type: String,
    @Json(name = "width")
    val width: Int,
    @Json(name = "height")
    val height: Int,
    @Json(name = "url")
    val imageUrl: String
)

@Entity(
    tableName = "LocalImage",
    primaryKeys = ["id", "ownerId", "album"]
)
@Parcelize
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
) : Parcelable
