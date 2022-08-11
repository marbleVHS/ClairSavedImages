package com.marblevhs.clairsavedimages.data

import com.google.gson.annotations.SerializedName


data class UserProfileResponse(
    @SerializedName("response")
    val userProfiles: List<UserProfile>
)

data class UserProfile(
    @SerializedName("id")
    val id: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("photo_400_orig")
    val profilePicUrl: String
)
