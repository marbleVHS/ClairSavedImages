package com.marblevhs.clairsavedimages

import android.app.Application
import com.marblevhs.clairsavedimages.di.AppComponent
import com.marblevhs.clairsavedimages.di.DaggerAppComponent

class MainApp : Application() {

    lateinit var appComponent: AppComponent


    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }


}