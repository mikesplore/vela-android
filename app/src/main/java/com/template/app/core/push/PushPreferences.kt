package com.template.app.core.push

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pushPrefs by preferencesDataStore(name = "push_prefs")

@Singleton
class PushPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val INSTALLATION_ID = stringPreferencesKey("installation_id")
        val LAST_FCM_TOKEN = stringPreferencesKey("last_fcm_token")
    }

    suspend fun getOrCreateInstallationId(): String {
        val existing = context.pushPrefs.data.map { it[Keys.INSTALLATION_ID] }.first()
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        context.pushPrefs.edit { it[Keys.INSTALLATION_ID] = created }
        return created
    }

    suspend fun getLastFcmToken(): String? =
        context.pushPrefs.data.map { it[Keys.LAST_FCM_TOKEN] }.first()

    suspend fun saveLastFcmToken(token: String) {
        context.pushPrefs.edit { it[Keys.LAST_FCM_TOKEN] = token }
    }

    suspend fun clearLastFcmToken() {
        context.pushPrefs.edit { it.remove(Keys.LAST_FCM_TOKEN) }
    }
}
