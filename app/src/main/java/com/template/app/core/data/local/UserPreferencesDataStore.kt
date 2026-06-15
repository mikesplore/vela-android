package com.template.app.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Type-safe DataStore wrapper. Add new preference keys here as your app grows.
 * Replace SharedPreferences entirely with this class.
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID    = stringPreferencesKey("user_id")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[Keys.AUTH_TOKEN] }
    val userId: Flow<String?>    = context.dataStore.data.map { it[Keys.USER_ID] }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[Keys.AUTH_TOKEN] = token }
    }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { it[Keys.USER_ID] = id }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
