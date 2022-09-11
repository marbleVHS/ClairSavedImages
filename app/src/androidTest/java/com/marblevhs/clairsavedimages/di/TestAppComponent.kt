package com.marblevhs.clairsavedimages.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [AppModule::class, TestAppBindModule::class])
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }


}