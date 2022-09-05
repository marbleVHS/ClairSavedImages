package com.marblevhs.clairsavedimages.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.marblevhs.clairsavedimages.di.AppScope
import com.marblevhs.clairsavedimages.di.EncryptedPreferences
import com.marblevhs.clairsavedimages.utils.SecurityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AppScope
class EncryptedDataStore @Inject constructor(
    @EncryptedPreferences private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {

    private val securityKeyAlias = "DataStoreKeyAlias"
    private val bytesToStringSeparator = "|"


    fun getSecuredStringFlow(KEY: Preferences.Key<String>) = dataStore.data
        .secureMap { preferences ->
            preferences[KEY]
        }

    suspend fun setSecuredString(KEY: Preferences.Key<String>, value: String?) {
        if (value.isNullOrEmpty()) {
            dataStore.edit { prefs ->
                prefs.remove(KEY)
            }
            return
        }
        dataStore.secureEdit(value) { prefs, encryptedValue ->
            prefs[KEY] = encryptedValue
        }
    }


    private suspend inline fun DataStore<Preferences>.secureEdit(
        value: String,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValue =
                security.encryptData(securityKeyAlias, value)
            editStore.invoke(it, encryptedValue.joinToString(bytesToStringSeparator))
        }
    }

    private inline fun Flow<Preferences>.secureMap(crossinline fetchValue: (value: Preferences) -> String?): Flow<String?> {
        return map { preferences ->
            val encryptedValue = fetchValue(preferences) ?: return@map null
            val decryptedValue = security.decryptData(
                securityKeyAlias,
                encryptedValue.split(bytesToStringSeparator).map { it.toByte() }.toByteArray()
            )
            decryptedValue
        }

    }
}