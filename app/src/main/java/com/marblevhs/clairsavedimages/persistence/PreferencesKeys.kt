package com.marblevhs.clairsavedimages.persistence

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val LAST_IMAGE_ID_KEY = stringPreferencesKey("LAST_IMAGE_ID")
val DEFAULT_NIGHT_MODE_KEY = intPreferencesKey("DEFAULT_NIGHT_MODE")
val TOKEN_KEY = stringPreferencesKey("TOKEN")