package com.marblevhs.clairsavedimages.data

import com.marblevhs.clairsavedimages.secrets.Secrets

enum class Album(val ownerId: String, val albumId: String){
    CLAIR(Secrets.CLAIR_ID, "saved"),
    PUBLIC(Secrets.PUBLIC_ID, "wall")
}