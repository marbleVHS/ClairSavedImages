package com.marblevhs.clairsavedimages.di

import com.marblevhs.clairsavedimages.loginScreen.VKLoginManager
import com.marblevhs.clairsavedimages.loginScreen.VKLoginManagerImpl
import com.marblevhs.clairsavedimages.repositories.*
import dagger.Binds
import dagger.Module

@Module
interface AppBindModule {

    @Binds
    @AppScope
    fun bindImagesRepo(repoImpl: ImagesRepoImpl): ImagesRepo

    @Binds
    @AppScope
    fun bindProfileRepo(repoImpl: ProfileRepoImpl): ProfileRepo

    @Binds
    @AppScope
    fun bindSettingsRepo(repoImpl: SettingsRepoImpl): SettingsRepo

    @Binds
    @AppScope
    fun bindVkLoginManager(vkLoginManagerImpl: VKLoginManagerImpl): VKLoginManager

}