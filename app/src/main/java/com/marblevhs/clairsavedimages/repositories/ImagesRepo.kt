package com.marblevhs.clairsavedimages.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.di.SimplePreferences
import com.marblevhs.clairsavedimages.network.ImageApiPagingSource
import com.marblevhs.clairsavedimages.network.ImageService
import com.marblevhs.clairsavedimages.persistence.EncryptedDataStore
import com.marblevhs.clairsavedimages.persistence.LAST_IMAGE_ID_KEY
import com.marblevhs.clairsavedimages.persistence.TOKEN_KEY
import com.marblevhs.clairsavedimages.persistence.room.DatabaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject


interface ImagesRepo {
    suspend fun updateLastImage()
    suspend fun isThereNewImages(): Boolean
    suspend fun getImagesPaging(album: Album, rev: Int): Flow<PagingData<LocalImage>>
    suspend fun getIsLiked(itemId: String, ownerId: String): Boolean
    suspend fun addLike(itemId: String, ownerId: String)
    suspend fun deleteLike(itemId: String, ownerId: String)
    suspend fun getFavs(rev: Int): List<LocalImage>
    suspend fun getIsFav(itemId: String, ownerId: String): Boolean
    suspend fun addFav(image: LocalImage)
    suspend fun deleteFav(image: LocalImage)
}

@AppScope
class ImagesRepoImpl @Inject constructor(
    @SimplePreferences private val dataStore: DataStore<Preferences>,
    private val encryptedPrefs: EncryptedDataStore,
    private val db: DatabaseStorage,
    private val imageService: ImageService,
) : ImagesRepo {

    override suspend fun updateLastImage() {
        try {
            val accessToken: String =
                encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
            val currentImageId = imageService.requestImages(
                ownerId = Album.DEBUG.ownerId,
                albumId = Album.DEBUG.albumId,
                count = 1,
                offset = 0,
                accessToken = accessToken
            ).imageResponse.images[0].id
            dataStore.edit { settings ->
                settings[LAST_IMAGE_ID_KEY] = currentImageId
            }
        } catch (e: Exception) {
            Log.e("RESP", "Couldn't save last image id")
        }

    }

    override suspend fun isThereNewImages(): Boolean {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        val lastImageId = dataStore.data.first()[LAST_IMAGE_ID_KEY] ?: "0"
        if (lastImageId == "0") {
            return false
        }
        val currentImageId = imageService.requestImages(
            ownerId = Album.DEBUG.ownerId,
            albumId = Album.DEBUG.albumId,
            count = 1,
            offset = 0,
            accessToken = accessToken
        ).imageResponse.images[0].id
        return lastImageId != currentImageId
    }

    override suspend fun getImagesPaging(album: Album, rev: Int): Flow<PagingData<LocalImage>> {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        return Pager(
            config = PagingConfig(
                pageSize = 400,
                initialLoadSize = 400,
                maxSize = 2000,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ImageApiPagingSource(
                    imageService = imageService,
                    album = album,
                    rev = rev,
                    accessToken = accessToken
                )
            }
        ).flow
    }

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        val isLiked = imageService.requestIsLiked(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        ).likeResponse.liked
        return (isLiked == 1)
    }

    override suspend fun addLike(itemId: String, ownerId: String) {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        imageService.requestAddLike(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        )
    }

    override suspend fun deleteLike(itemId: String, ownerId: String) {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        imageService.requestDeleteLike(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        )
    }

    override suspend fun getFavs(rev: Int): List<LocalImage> {
        var favs = db.imageDao().allImage
        if (rev == 1) {
            favs = favs.asReversed()
        }
        return favs
    }

    override suspend fun getIsFav(itemId: String, ownerId: String): Boolean {
        val isFav = db.imageDao().getImageByImageId(itemId)
        return isFav != null
    }

    override suspend fun addFav(image: LocalImage) {
        db.imageDao().insert(image = image)
    }

    override suspend fun deleteFav(image: LocalImage) {
        db.imageDao().delete(image = image)
    }

}