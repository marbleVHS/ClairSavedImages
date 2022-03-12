package com.marblevhs.clairsavedimages.data


internal fun JsonImage.toLocalImage(ownerId: String, album: String): LocalImage {
    return LocalImage(
        id = this.id,
        ownerId = ownerId,
        album = album,
        width = this.sizes[this.sizes.size - 1].width,
        height = this.sizes[this.sizes.size - 1].height,
        thumbnailUrl = this.sizes[2].imageUrl,
        fullSizeUrl = this.sizes[this.sizes.size - 1].imageUrl
    )
}