package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.ConnectionSettings

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val themeMode: String = AppThemeMode.SYSTEM.name,
    val biometricsEnabled: Boolean = false
) {
    fun toDomain() = ConnectionSettings(
        themeMode = runCatching { AppThemeMode.valueOf(themeMode) }.getOrDefault(AppThemeMode.SYSTEM),
        biometricsEnabled = biometricsEnabled
    )

    companion object {
        fun fromDomain(domain: ConnectionSettings) = SettingsEntity(
            themeMode = domain.themeMode.name,
            biometricsEnabled = domain.biometricsEnabled
        )
    }
}
