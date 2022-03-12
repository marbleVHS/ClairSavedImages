package com.marblevhs.clairsavedimages.di


import android.content.Context
import androidx.room.Room
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import com.marblevhs.clairsavedimages.imageRepo.Repo
import com.marblevhs.clairsavedimages.imageRepo.RepoImpl
import com.marblevhs.clairsavedimages.network.ImageApi
import com.marblevhs.clairsavedimages.room.DatabaseStorage
import com.marblevhs.clairsavedimages.secrets.Secrets
import dagger.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Scope

@Scope
annotation class AppScope

@AppScope
@Component(modules = [AppModule::class])
interface AppComponent{

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance context: Context): AppComponent
    }


    fun inject(imageListFragment: ImageListFragment)
    fun inject(favouritesListFragment: FavouritesListFragment)
    fun inject(imageDetailsFragment: ImageDetailsFragment)
}

@Module(includes = [AppBindModule::class])
object AppModule{

    @Provides
    @AppScope
    fun provideImageApi(): ImageApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(Secrets.BASE_URL)
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

}

@Module
interface AppBindModule{
    @Binds
    @AppScope
    fun bindRepo(repoImpl: RepoImpl): Repo
}