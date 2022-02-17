package com.marblevhs.clairsavedimages.di


import com.marblevhs.clairsavedimages.BuildConfig
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import com.marblevhs.clairsavedimages.imageRepo.Repo
import com.marblevhs.clairsavedimages.imageRepo.RepoImpl
import com.marblevhs.clairsavedimages.network.ImageApi
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
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
        fun create(): AppComponent
    }

    fun inject(mainViewModel: MainViewModel)
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
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

}

@Module
interface AppBindModule{
    @Binds
    @AppScope
    fun bindRepo(repoImpl: RepoImpl): Repo
}