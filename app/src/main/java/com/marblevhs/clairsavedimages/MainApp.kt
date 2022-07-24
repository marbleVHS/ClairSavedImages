package com.marblevhs.clairsavedimages

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.marblevhs.clairsavedimages.di.AppComponent
import com.marblevhs.clairsavedimages.di.DaggerAppComponent

class MainApp : Application() {

    lateinit var appComponent: AppComponent


    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
        createNotificationChannel()
    }


    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workerChannelName = "New images notifications"
            val workerChannelDescriptionText = "Notifications about new images"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val workerChannel =
                NotificationChannel("FetchingWorkerID", workerChannelName, importance).apply {
                    description = workerChannelDescriptionText
                }

            val firebaseChannelName = "Push notifications"
            val firebaseChannel =
                NotificationChannel("FirebaseChannelID", firebaseChannelName, importance)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(workerChannel)
            notificationManager.createNotificationChannel(firebaseChannel)
        }
    }


}