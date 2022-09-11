package com.marblevhs.clairsavedimages

import com.marblevhs.clairsavedimages.di.DaggerTestAppComponent

class TestMainApp : MainApp() {

    override fun initializeDaggerComponent() {
        appComponent = DaggerTestAppComponent.factory().create(this)
    }

}