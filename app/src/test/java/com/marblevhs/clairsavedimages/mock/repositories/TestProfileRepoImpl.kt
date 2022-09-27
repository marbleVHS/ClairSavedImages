package com.marblevhs.clairsavedimages.mock.repositories

import com.marblevhs.clairsavedimages.data.UserProfile
import com.marblevhs.clairsavedimages.repositories.ProfileRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TestProfileRepoImpl @Inject constructor() : ProfileRepo {

    override fun getProfileFlow(): Flow<UserProfile> {
        TODO("Not yet implemented")
    }

    override suspend fun getProfile(): UserProfile {
        TODO("Not yet implemented")
    }

    override fun getIsLoggedFlow(): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun saveAccessToken(accessToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clearLoginData() {
        TODO("Not yet implemented")
    }

    override suspend fun registerUserToServer() {
        TODO("Not yet implemented")
    }

    override suspend fun sendFCMRegistrationToServer(token: String) {
        TODO("Not yet implemented")
    }

}