package com.marblevhs.clairsavedimages.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marblevhs.clairsavedimages.repositories.ProfileRepo
import com.marblevhs.clairsavedimages.utils.appComponent
import javax.inject.Inject

class FCMRegistrationWorker constructor(
    appContext: Context,
    params: WorkerParameters,
) :
    CoroutineWorker(
        appContext,
        params
    ) {

    @Inject
    lateinit var profileRepo: ProfileRepo


    override suspend fun doWork(): Result {
        return try {
            this.applicationContext.appComponent.inject(this)
            val token = inputData.getString("token")!!

            profileRepo.sendFCMRegistrationToServer(token)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}