package com.marblevhs.clairsavedimages.mock.repositories

import com.marblevhs.clairsavedimages.repositories.SettingsRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TestSettingsRepoImpl @Inject constructor() : SettingsRepo {

    override suspend fun setDefaultNightMode(defaultNightMode: Int) {
        TODO("Not yet implemented")
    }

    override fun getDefaultNightModeFlow(): Flow<Int> {
        TODO("Not yet implemented")
    }

}