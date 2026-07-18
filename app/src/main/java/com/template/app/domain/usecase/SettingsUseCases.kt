package com.template.app.domain.usecase

import com.template.app.core.utils.Resource
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings
import com.template.app.domain.model.VelaConfig
import com.template.app.domain.repository.ConfigRepository
import com.template.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<ConnectionSettings> = repository.observeSettings()
}

class SaveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun updateTheme(themeMode: AppThemeMode) {
        repository.saveTheme(themeMode)
    }
}

class FetchVelaConfigUseCase @Inject constructor(
    private val velaRepository: ConfigRepository
) {
    suspend operator fun invoke(): Resource<VelaConfig> = velaRepository.getConfig()
}

class ObserveVelaConfigUseCase @Inject constructor(
    private val velaRepository: ConfigRepository
) {
    operator fun invoke(): Flow<VelaConfig?> = velaRepository.observeConfig()
}
