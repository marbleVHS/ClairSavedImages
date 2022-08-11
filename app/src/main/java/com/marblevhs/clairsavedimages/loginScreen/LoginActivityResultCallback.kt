package com.marblevhs.clairsavedimages.loginScreen

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.marblevhs.clairsavedimages.MainViewModel
import com.marblevhs.clairsavedimages.workers.UserRegistrationWorker
import com.vk.api.sdk.auth.VKAuthenticationResult


class LoginActivityResultCallback(
    private val viewModel: MainViewModel,
    private val context: Context
) :
    ActivityResultCallback<VKAuthenticationResult> {
    override fun onActivityResult(result: VKAuthenticationResult?) {
        if (result is VKAuthenticationResult.Success) {
            viewModel.saveAccessToken(result.token.accessToken)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val work =
                OneTimeWorkRequestBuilder<UserRegistrationWorker>().setConstraints(constraints)
                    .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(
                work
            )
        }
        if (result is VKAuthenticationResult.Failed) {
            Log.e("RESP", result.exception.authError.toString())
        }
    }

}