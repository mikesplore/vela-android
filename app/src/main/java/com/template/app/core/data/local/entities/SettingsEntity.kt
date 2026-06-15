package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.ConnectionSettings

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0, // Singleton pattern for settings
    val baseUrl: String,
    val apiToken: String,
    val onboardingComplete: Boolean
) {
    fun toDomain() = ConnectionSettings(
        baseUrl = baseUrl,
        apiToken = apiToken,
        onboardingComplete = onboardingComplete
    )

    companion object {
        fun fromDomain(domain: ConnectionSettings) = SettingsEntity(
            baseUrl = domain.baseUrl,
            apiToken = domain.apiToken,
            onboardingComplete = domain.onboardingComplete
        )
    }

    // Extension function to easily map a Domain model to a Database Entity
    fun ConnectionSettings.toEntity(): SettingsEntity {
        return SettingsEntity(
            baseUrl = this.baseUrl,
            apiToken = this.apiToken,
            onboardingComplete = this.onboardingComplete
        )
    }
}
