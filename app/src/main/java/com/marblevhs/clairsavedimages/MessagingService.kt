package com.marblevhs.clairsavedimages

import android.annotation.SuppressLint
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.marblevhs.clairsavedimages.utils.appComponent
import com.marblevhs.clairsavedimages.workers.FCMRegistrationWorker


class MessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        this.appComponent.inject(this)
        super.onCreate()
    }

    @SuppressLint("RestrictedApi")
    override fun onNewToken(token: String) {
        super.onNewToken(token)


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work =
            OneTimeWorkRequestBuilder<FCMRegistrationWorker>()
                .setInputData(Data(mapOf("token" to token)))
                .setConstraints(constraints)
                .build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(
            work
        )

    }


}