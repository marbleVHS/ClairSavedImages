package com.marblevhs.clairsavedimages.di

import android.content.Context
import com.marblevhs.clairsavedimages.MainActivity
import com.marblevhs.clairsavedimages.MessagingService
import com.marblevhs.clairsavedimages.favouritesList.FavouritesListFragment
import com.marblevhs.clairsavedimages.imageDetails.ImageDetailsFragment
import com.marblevhs.clairsavedimages.imageList.ImageListFragment
import com.marblevhs.clairsavedimages.profileScreen.ProfileFragment
import com.marblevhs.clairsavedimages.workers.FCMRegistrationWorker
import com.marblevhs.clairsavedimages.workers.FetchingWorker
import com.marblevhs.clairsavedimages.workers.UserRegistrationWorker
import dagger.BindsInstance
import dagger.Component


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