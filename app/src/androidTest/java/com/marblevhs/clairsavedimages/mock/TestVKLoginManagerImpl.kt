package com.marblevhs.clairsavedimages.mock

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.loginScreen.VKLoginManager
import com.marblevhs.clairsavedimages.persistence.EncryptedDataStore
import com.marblevhs.clairsavedimages.persistence.TOKEN_KEY
import com.marblevhs.clairsavedimages.test.BuildConfig
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AppScope
class TestVKLoginManagerImpl @Inject constructor(
    private val encryptedDataStore: EncryptedDataStore
) : VKLoginManager {

    override fun registerActivityResultLauncher(
        activity: ComponentActivity,
        callback: ActivityResultCallback<VKAuthenticationResult>,
        vkScopeCollection: Collection<VKScope>
    ) {

    }

    override fun login() {
        runBlocking {
            encryptedDataStore.setSecuredString(
                TOKEN_KEY,
                BuildConfig.TEST_TOKEN
            )
        }
    }
}
