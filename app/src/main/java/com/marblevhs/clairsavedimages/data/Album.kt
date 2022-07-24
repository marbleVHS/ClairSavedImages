package com.marblevhs.clairsavedimages.data

import com.marblevhs.clairsavedimages.notSecrets.NotSecrets

enum class Album(val ownerId: String, val albumId: String) {
    DEBUG(NotSecrets.DEBUG_ID, "saved"),
    CLAIR(NotSecrets.CLAIR_ID, "saved"),
    PUBLIC(NotSecrets.PUBLIC_ID, "wall"),
}