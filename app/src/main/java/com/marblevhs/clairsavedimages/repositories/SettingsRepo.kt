package com.marblevhs.clairsavedimages.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.di.SimplePreferences
import com.marblevhs.clairsavedimages.persistence.DEFAULT_NIGHT_MODE_KEY
import com.marblevhs.clairsavedimages.persistence.FETCHING_NOTIFICATION_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


interface SettingsRepo {
    suspend fun setDefaultNightMode(defaultNightMode: Int)
    fun getDefaultNightModeFlow(): Flow<Int>
    suspend fun isNotificationsEnabled(): Boolean
    fun getNotificationsToggleFlow(): Flow<Boolean>
    suspend fun setNotificationToggle(value: Boolean)
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

    override fun getNotificationsToggleFlow(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[FETCHING_NOTIFICATION_KEY] ?: false
        }
    }

    override suspend fun isNotificationsEnabled(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[FETCHING_NOTIFICATION_KEY] ?: false
        }.first()
    }

    override suspend fun setNotificationToggle(value: Boolean) {
        dataStore.edit { settings ->
            settings[FETCHING_NOTIFICATION_KEY] = value
        }
    }
}