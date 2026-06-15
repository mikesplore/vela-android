package com.template.app.domain.repository

import com.template.app.domain.model.ConnectionSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Observe settings changes reactively */
    fun observeSettings(): Flow<ConnectionSettings>
    
    /** One-shot get for the current settings */
    suspend fun getSettings(): ConnectionSettings
    
    /** Save new settings (handles formatting like trimming URLs) */
    suspend fun saveSettings(settings: ConnectionSettings)
    
    /** Clear all connection credentials and reset onboarding */
    suspend fun clearSettings()
    
    /** Mark onboarding as complete without changing other settings */
    suspend fun completeOnboarding()
}
