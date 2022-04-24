package com.marblevhs.clairsavedimages.di


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import com.marblevhs.clairsavedimages.imageRepo.Repo
import com.marblevhs.clairsavedimages.imageRepo.RepoImpl
import com.marblevhs.clairsavedimages.network.ImageService
import com.marblevhs.clairsavedimages.network.ProfileService
import com.marblevhs.clairsavedimages.profileScreen.ProfileFragment
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import dagger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
}

@Module(includes = [AppBindModule::class])
object AppModule {

    @Provides
    @AppScope
    fun provideImageService(): ImageService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    @Provides
    @AppScope
    fun provideProfileService(): ProfileService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/")
            .addConverterFactory(GsonConverterFactory.create())
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