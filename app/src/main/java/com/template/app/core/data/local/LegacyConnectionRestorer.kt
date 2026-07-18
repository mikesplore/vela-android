package com.template.app.core.data.local

import android.content.Context
import android.util.Log
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.repository.DeviceRepository
import com.template.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyConnectionRestorer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val settingsRepository: SettingsRepository
) {
    private var restored = false

    suspend fun restoreIfNeeded() {
        if (restored) return
        restored = true

        val captured = LegacyConnectionMigrator.consumePending(context) ?: return
        Log.i("LegacyConnectionRestorer", "Restoring migrated connection")

        runCatching {
            settingsRepository.saveTheme(
                runCatching { AppThemeMode.valueOf(captured.themeMode) }
                    .getOrDefault(AppThemeMode.SYSTEM)
            )
        }

        if (captured.baseUrl.isNotBlank() && captured.apiToken.isNotBlank()) {
            if (!deviceRepository.hasDevices()) {
                deviceRepository.addOrUpdateDevice(
                    agentId = "migrated-${captured.baseUrl.hashCode()}",
                    relayBaseUrl = captured.baseUrl,
                    relaySecret = captured.apiToken,
                    label = captured.hostname ?: "My Device",
                    hostname = captured.hostname
                )
            }
        }
    }
}
