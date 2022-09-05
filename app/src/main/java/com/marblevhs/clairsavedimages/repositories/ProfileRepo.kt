package com.marblevhs.clairsavedimages.repositories

import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.network.HerokuService
import com.marblevhs.clairsavedimages.network.ProfileService
import com.marblevhs.clairsavedimages.persistence.EncryptedDataStore
import com.marblevhs.clairsavedimages.persistence.TOKEN_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject


interface ProfileRepo {
    suspend fun getProfile(): UserProfile
    fun getIsLoggedFlow(): Flow<Boolean>
    suspend fun saveAccessToken(accessToken: String)
    suspend fun clearLoginData()
    suspend fun registerUserToServer()
    suspend fun sendFCMRegistrationToServer(token: String)
}

@AppScope
class ProfileRepoImpl @Inject constructor(
    private val encryptedPrefs: EncryptedDataStore,
    private val profileService: ProfileService,
    private val herokuService: HerokuService,
) : ProfileRepo {

    override suspend fun getProfile(): UserProfile {
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        val userProfile = profileService.requestProfileInfo(
            accessToken = accessToken
        ).userProfiles[0]
        return userProfile
    }

    override fun getIsLoggedFlow(): Flow<Boolean> =
        encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).map { token ->
            token != null
        }

    override suspend fun saveAccessToken(accessToken: String) {
        encryptedPrefs.setSecuredString(TOKEN_KEY, accessToken)
    }

    override suspend fun clearLoginData() {
        encryptedPrefs.setSecuredString(TOKEN_KEY, null)
    }

    override suspend fun registerUserToServer() {
        val profile = getProfile()
        val accessToken: String =
            encryptedPrefs.getSecuredStringFlow(TOKEN_KEY).firstOrNull() ?: "0"
        herokuService.registerUser(
            id = profile.id,
            firstName = profile.firstName,
            lastName = profile.lastName,
            profilePicUrl = profile.profilePicUrl,
            token = accessToken
        )
    }

    override suspend fun sendFCMRegistrationToServer(token: String) {
        val profile = getProfile()
        herokuService.registerFCMToken(profile.id, token)
    }

}