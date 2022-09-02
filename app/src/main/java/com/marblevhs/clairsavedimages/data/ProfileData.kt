package com.marblevhs.clairsavedimages.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    @Json(name = "response")
    val userProfiles: List<UserProfile>
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "id")
    val id: Int,
    @Json(name = "first_name")
    val firstName: String,
    @Json(name = "last_name")
    val lastName: String,
    @Json(name = "photo_400_orig")
    val profilePicUrl: String
)
