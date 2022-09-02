package com.marblevhs.clairsavedimages.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marblevhs.clairsavedimages.extensions.appComponent
import com.marblevhs.clairsavedimages.monoRepo.Repo
import javax.inject.Inject

class UserRegistrationWorker constructor(
    appContext: Context,
    params: WorkerParameters,
) :
    CoroutineWorker(
        appContext,
        params
    ) {

    @Inject
    lateinit var repo: Repo


    override suspend fun doWork(): Result {
        return try {
            this.applicationContext.appComponent.inject(this)
            repo.registerUserToServer()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}