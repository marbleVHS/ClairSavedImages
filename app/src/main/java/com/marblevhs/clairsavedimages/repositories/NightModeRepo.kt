package com.marblevhs.clairsavedimages.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.di.SimplePreferences
import com.marblevhs.clairsavedimages.persistence.DEFAULT_NIGHT_MODE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


interface SettingsRepo {
    suspend fun setDefaultNightMode(defaultNightMode: Int)
    fun getDefaultNightModeFlow(): Flow<Int>
}

@AppScope
class SettingsRepoImpl @Inject constructor(
    @SimplePreferences private val dataStore: DataStore<Preferences>,
) : SettingsRepo {

    override suspend fun setDefaultNightMode(defaultNightMode: Int) {
        dataStore.edit { settings ->
            settings[DEFAULT_NIGHT_MODE_KEY] = defaultNightMode
        }
    }

    override fun getDefaultNightModeFlow(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[DEFAULT_NIGHT_MODE_KEY] ?: -1
        }
    }

}