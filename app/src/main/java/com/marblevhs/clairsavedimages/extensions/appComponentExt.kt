package com.marblevhs.clairsavedimages.extensions

import android.content.Context
import com.marblevhs.clairsavedimages.MainApp
import com.marblevhs.clairsavedimages.di.AppComponent

val Context.appComponent: AppComponent
    get() = when (this) {
        is MainApp -> appComponent
        else -> this.applicationContext.appComponent
    }