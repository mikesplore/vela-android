package com.template.app.domain.model

data class PairedDevice(
    val id: Long = 0,
    val agentId: String,
    val relayBaseUrl: String,
    val relaySecret: String,
    val label: String,
    val hostname: String? = null,
    val username: String? = null,
    val vpsUrl: String? = null,
    val pairedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
) {
    val displayName: String
        get() = label.ifBlank { hostname ?: username ?: "Device" }
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)
