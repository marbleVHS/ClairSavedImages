package com.marblevhs.clairsavedimages.loginScreen

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import com.marblevhs.clairsavedimages.di.AppScope
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import javax.inject.Inject


interface VKLoginManager {
    fun registerActivityResultLauncher(
        activity: ComponentActivity,
        callback: ActivityResultCallback<VKAuthenticationResult>,
        vkScopeCollection: Collection<VKScope>
    )

    fun login()
}

@AppScope
class VKLoginManagerImpl @Inject constructor() : VKLoginManager {

    private var activityResultLauncher: ActivityResultLauncher<Collection<VKScope>>? = null
    private var vkScope: Collection<VKScope>? = null

    override fun registerActivityResultLauncher(
        activity: ComponentActivity,
        callback: ActivityResultCallback<VKAuthenticationResult>,
        vkScopeCollection: Collection<VKScope>
    ) {
        activityResultLauncher = VK.login(activity, callback)
        vkScope = vkScopeCollection
    }

    override fun login() {
        activityResultLauncher?.launch(vkScope)
    }

}