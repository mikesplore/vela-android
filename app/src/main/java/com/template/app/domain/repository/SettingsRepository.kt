package com.template.app.domain.repository

import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<ConnectionSettings>
    suspend fun getSettings(): ConnectionSettings
    suspend fun saveTheme(themeMode: AppThemeMode)
}
