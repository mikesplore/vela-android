package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.SettingsDao
import com.template.app.core.data.local.entities.SettingsEntity
import com.template.app.core.security.BiometricCredentialStore
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings
import com.template.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val credentialStore: BiometricCredentialStore
) : SettingsRepository {

    override fun observeSettings(): Flow<ConnectionSettings> =
        settingsDao.observeSettings().map { it?.toDomain() ?: ConnectionSettings() }

    override suspend fun getSettings(): ConnectionSettings =
        settingsDao.getSettings()?.toDomain() ?: ConnectionSettings()

    override suspend fun saveTheme(themeMode: AppThemeMode) {
        val current = getSettings()
        settingsDao.upsert(
            SettingsEntity.fromDomain(current.copy(themeMode = themeMode))
        )
    }

    override suspend fun enableBiometrics(pin: String) {
        credentialStore.savePin(pin)
        val current = getSettings()
        settingsDao.upsert(
            SettingsEntity.fromDomain(current.copy(biometricsEnabled = true))
        )
    }

    override suspend fun disableBiometrics() {
        credentialStore.clear()
        val current = getSettings()
        settingsDao.upsert(
            SettingsEntity.fromDomain(current.copy(biometricsEnabled = false))
        )
    }

    override suspend fun updateBiometricPin(pin: String) {
        credentialStore.savePin(pin)
    }

    override fun getStoredPin(): String? = credentialStore.getPin()
}
