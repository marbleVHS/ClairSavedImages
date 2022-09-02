package com.marblevhs.clairsavedimages.loginScreen

import android.util.Log
import androidx.activity.result.ActivityResultCallback
import com.marblevhs.clairsavedimages.MainViewModel
import com.vk.api.sdk.auth.VKAuthenticationResult


class LoginActivityResultCallback(private val viewModel: MainViewModel) :
    ActivityResultCallback<VKAuthenticationResult> {
    override fun onActivityResult(result: VKAuthenticationResult?) {
        if (result is VKAuthenticationResult.Success) {
            viewModel.saveAccessToken(result.token.accessToken)
        }
        if (result is VKAuthenticationResult.Failed) {
            Log.e("RESP", result.exception.authError.toString())
        }
    }

}