package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.SettingsDao
import com.template.app.core.data.local.entities.SettingsEntity
import com.template.app.domain.model.ConnectionSettings
import com.template.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun observeSettings(): Flow<ConnectionSettings> =
        settingsDao.observeSettings().map { it?.toDomain() ?: ConnectionSettings() }

    override suspend fun getSettings(): ConnectionSettings =
        settingsDao.getSettings()?.toDomain() ?: ConnectionSettings()

    override suspend fun saveSettings(settings: ConnectionSettings) {
        val sanitized = settings.copy(
            baseUrl = settings.baseUrl.trim().removeSuffix("/"),
            apiToken = settings.apiToken.trim()
        )
        settingsDao.upsert(SettingsEntity.fromDomain(sanitized))
    }

    override suspend fun clearSettings() {
        settingsDao.deleteSettings()
    }

    override suspend fun completeOnboarding() {
        val current = getSettings()
        saveSettings(current.copy(onboardingComplete = true))
    }
}
