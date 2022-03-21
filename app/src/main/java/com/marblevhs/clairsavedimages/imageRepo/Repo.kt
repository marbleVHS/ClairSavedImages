package com.marblevhs.clairsavedimages.imageRepo


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.marblevhs.clairsavedimages.data.LocalImage
import com.marblevhs.clairsavedimages.network.ImageApi
import com.marblevhs.clairsavedimages.network.ImageApiPagingSource
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface Repo {
    suspend fun getImagesPaging(query: String): Flow<PagingData<LocalImage>>
    suspend fun getIsLiked(itemId: String, ownerId: String): Boolean
    suspend fun getIsFav(itemId: String, ownerId: String): Boolean
    suspend fun addLike(itemId: String, ownerId: String)
    suspend fun deleteLike(itemId: String, ownerId: String)
    suspend fun getFavs(rev: Int): List<LocalImage>
    suspend fun addFav(image: LocalImage)
    suspend fun deleteFav(image: LocalImage)
    suspend fun getIsLogged(): Boolean
    suspend fun saveAccessToken(accessKey: String)
    suspend fun clearLoginData()

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val ACCESS_TOKEN_KEY = stringPreferencesKey("ACCESS_TOKEN")

class RepoImpl @Inject constructor(
    private val api: ImageApi,
    private val db: DatabaseStorage,
    private val dataStore: DataStore<Preferences>
) : Repo {


    override suspend fun getImagesPaging(query: String): Flow<PagingData<LocalImage>> {
        val accessToken: String = dataStore.data.first()[ACCESS_TOKEN_KEY] ?: "0"
        return Pager(
            config = PagingConfig(
                pageSize = 400,
                initialLoadSize = 400,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ImageApiPagingSource(
                    imageApi = api,
                    query = query,
                    accessToken = accessToken
                )
            }
        ).flow
    }

    override suspend fun getIsLogged(): Boolean {
        val accessKey = dataStore.data.first()[ACCESS_TOKEN_KEY] ?: "0"
        return accessKey != "0"
    }

    override suspend fun clearLoginData() {
        dataStore.edit { settings ->
            settings[ACCESS_TOKEN_KEY] = "0"
        }
    }

    override suspend fun saveAccessToken(accessKey: String) {
        dataStore.edit { settings ->
            settings[ACCESS_TOKEN_KEY] = accessKey
        }
    }

    override suspend fun getIsLiked(itemId: String, ownerId: String): Boolean {
        val accessToken: String = dataStore.data.first()[ACCESS_TOKEN_KEY] ?: "0"
        val isLiked = api.requestIsLiked(
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
        val accessToken: String = dataStore.data.first()[ACCESS_TOKEN_KEY] ?: "0"
        api.requestAddLike(
            ownerId = ownerId,
            accessToken = accessToken,
            itemId = itemId
        )
    }

    override suspend fun deleteLike(itemId: String, ownerId: String) {
        val accessToken: String = dataStore.data.first()[ACCESS_TOKEN_KEY] ?: "0"
        api.requestDeleteLike(
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