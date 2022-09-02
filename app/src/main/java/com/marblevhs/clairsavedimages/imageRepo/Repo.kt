package com.marblevhs.clairsavedimages.imageRepo


import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.marblevhs.clairsavedimages.data.Album
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.network.ImageApiPagingSource
import com.marblevhs.clairsavedimages.network.ImageService
import com.marblevhs.clairsavedimages.network.ProfileService
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface Repo {

    suspend fun getImagesPaging(album: Album, rev: Int): Flow<PagingData<LocalImage>>
    suspend fun getIsLiked(itemId: String, ownerId: String): Boolean
    suspend fun getIsFav(itemId: String, ownerId: String): Boolean
    suspend fun addLike(itemId: String, ownerId: String)
    suspend fun deleteLike(itemId: String, ownerId: String)
    suspend fun getFavs(rev: Int): List<LocalImage>
    suspend fun addFav(image: LocalImage)
    suspend fun deleteFav(image: LocalImage)
    suspend fun getIsLogged(): Boolean
    suspend fun getProfile(): UserProfile
    suspend fun saveAccessToken(accessKey: String)
    suspend fun clearLoginData()
    suspend fun getDefaultNightMode(): Int
    suspend fun setDefaultNightMode(defaultNightMode: Int)
    suspend fun isThereNewImages(): Boolean
    suspend fun updateLastImage()

}


val LAST_IMAGE_ID_KEY = stringPreferencesKey("LAST_IMAGE_ID")
val DEFAULT_NIGHT_MODE_KEY = intPreferencesKey("DEFAULT_NIGHT_MODE")

class RepoImpl @Inject constructor(
    private val imageService: ImageService,
    private val profileService: ProfileService,
    private val db: DatabaseStorage,
    private val dataStore: DataStore<Preferences>,
    private val encryptedPrefs: SharedPreferences
) : Repo {


    override suspend fun getImagesPaging(album: Album, rev: Int): Flow<PagingData<LocalImage>> {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
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

    override suspend fun updateLastImage() {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        val currentImageId = imageService.requestImages(
            ownerId = Album.CLAIR.ownerId,
            albumId = Album.CLAIR.albumId,
            count = 1,
            offset = 0,
            accessToken = accessToken
        ).imageResponse.images[0].id
        dataStore.edit { settings ->
            settings[LAST_IMAGE_ID_KEY] = currentImageId
        }
    }

    override suspend fun getIsLogged(): Boolean {
        val accessKeyPrefs = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        return accessKeyPrefs != "0"
    }

    override suspend fun getDefaultNightMode(): Int {
        return dataStore.data.first()[DEFAULT_NIGHT_MODE_KEY] ?: -1
    }

    override suspend fun setDefaultNightMode(defaultNightMode: Int) {
        dataStore.edit { settings ->
            settings[DEFAULT_NIGHT_MODE_KEY] = defaultNightMode
        }
    }

    override suspend fun isThereNewImages(): Boolean {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        val lastImageId = dataStore.data.first()[LAST_IMAGE_ID_KEY] ?: "0"
        if (lastImageId == "0") {
            return false
        }
        val currentImageId = imageService.requestImages(
            ownerId = Album.CLAIR.ownerId,
            albumId = Album.CLAIR.albumId,
            count = 1,
            offset = 0,
            accessToken = accessToken
        ).imageResponse.images[0].id
        return lastImageId != currentImageId
    }


    override suspend fun getProfile(): UserProfile {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        val userProfile = profileService.requestProfileInfo(
            accessToken = accessToken
        ).userProfiles[0]
        return userProfile
    }

    override suspend fun clearLoginData() {
        with(encryptedPrefs.edit()) {
            putString("ACCESS_TOKEN_KEY", "0")
            apply()
        }
    }

    override suspend fun saveAccessToken(accessKey: String) {
        with(encryptedPrefs.edit()) {
            this.putString("ACCESS_TOKEN_KEY", accessKey)
            apply()
        }
    }

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        val isLiked = imageService.requestIsLiked(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        ).likeResponse.liked
        return (isLiked == 1)
    }

    override suspend fun getIsFav(itemId: String, ownerId: String): Boolean {
        val isFav = db.imageDao().getImageByImageId(itemId)
        return isFav != null
    }

    override suspend fun addLike(itemId: String, ownerId: String) {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
        imageService.requestAddLike(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        )
    }

    override suspend fun deleteLike(itemId: String, ownerId: String) {
        val accessToken: String = encryptedPrefs.getString("ACCESS_TOKEN_KEY", null) ?: "0"
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

    override suspend fun addFav(image: LocalImage) {
        db.imageDao().insert(image = image)
    }

    override suspend fun deleteFav(image: LocalImage) {
        db.imageDao().delete(image = image)
    }

}