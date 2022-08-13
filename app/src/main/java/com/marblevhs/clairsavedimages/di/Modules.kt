package com.marblevhs.clairsavedimages.di


import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.MessagingService
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import com.marblevhs.clairsavedimages.imageRepo.Repo
import com.marblevhs.clairsavedimages.imageRepo.RepoImpl
import com.marblevhs.clairsavedimages.network.HerokuService
import com.marblevhs.clairsavedimages.network.ImageService
import com.marblevhs.clairsavedimages.network.ProfileService
import com.marblevhs.clairsavedimages.profileScreen.ProfileFragment
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import com.marblevhs.clairsavedimages.workers.FCMRegistrationWorker
import com.marblevhs.clairsavedimages.workers.FetchingWorker
import com.marblevhs.clairsavedimages.workers.UserRegistrationWorker
import com.squareup.moshi.Moshi
import dagger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Scope

@Scope
annotation class AppScope

@AppScope
@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }


    fun inject(imageListFragment: ImageListFragment)
    fun inject(favouritesListFragment: FavouritesListFragment)
    fun inject(imageDetailsFragment: ImageDetailsFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(profileFragment: ProfileFragment)
    fun inject(fetchingWorker: FetchingWorker)
    fun inject(userRegistrationWorker: UserRegistrationWorker)
    fun inject(messagingService: MessagingService)
    fun inject(fcmRegistrationWorker: FCMRegistrationWorker)
}

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
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient()
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
    fun provideEncryptedSharedPreferences(context: Context): SharedPreferences {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            "encryptedPrefs",
            keyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences
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
    fun providePreferencesDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

}

@Module
interface AppBindModule {
    @Binds
    @AppScope
    fun bindRepo(repoImpl: RepoImpl): Repo
}