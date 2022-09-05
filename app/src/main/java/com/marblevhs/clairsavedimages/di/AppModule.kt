package com.marblevhs.clairsavedimages.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.marblevhs.clairsavedimages.network.HerokuService
import com.marblevhs.clairsavedimages.network.ImageService
import com.marblevhs.clairsavedimages.network.ProfileService
import com.marblevhs.clairsavedimages.persistence.room.DatabaseStorage
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.io.File

@Module(includes = [AppBindModule::class])
object AppModule {

    @Provides
    @AppScope
    fun provideImageService(client: OkHttpClient, moshi: Moshi): ImageService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create()
    }

    @Provides
    @AppScope
    fun provideSerializer(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @AppScope
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = 40L * 1024L * 1024L
                )
            ).build()
    }

    @Provides
    @AppScope
    fun provideHerokuService(client: OkHttpClient, moshi: Moshi): HerokuService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://clairesavedimages-backend.herokuapp.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create()
    }


    @Provides
    @AppScope
    fun provideProfileService(client: OkHttpClient, moshi: Moshi): ProfileService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create()
    }

    @Provides
    @AppScope
    fun provideRoomDB(context: Context): DatabaseStorage {
        val db = Room.databaseBuilder(
            context,
            DatabaseStorage::class.java,
            "favourites.db"
        ).build()
        return db
    }

    @Provides
    @AppScope
    @SimplePreferences
    fun providePreferencesDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @AppScope
    @EncryptedPreferences
    fun provideEncryptedPreferencesDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("encryptedPrefs") }
        )
    }

}