package com.marblevhs.clairsavedimages.mock.repositories

import androidx.paging.PagingData
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.repositories.ImagesRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TestImagesRepoImpl @Inject constructor() : ImagesRepo {

    override suspend fun updateLastImage() {
        TODO("Not yet implemented")
    }

    override suspend fun isThereNewImages(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getImagesPaging(album: Album, rev: Int): Flow<PagingData<LocalImage>> {
        TODO("Not yet implemented")
    }

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        return true
    }

    override suspend fun addLike(itemId: String, ownerId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLike(itemId: String, ownerId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getFavs(rev: Int): List<LocalImage> {
        TODO("Not yet implemented")
    }

    override suspend fun getIsFav(itemId: String, ownerId: String): Boolean {
        return true
    }

    override suspend fun addFav(image: LocalImage) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFav(image: LocalImage) {
        TODO("Not yet implemented")
    }


}