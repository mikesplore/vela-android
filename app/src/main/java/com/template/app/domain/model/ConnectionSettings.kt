package com.template.app.domain.model

enum class AppThemeMode {
    LIGHT, DARK, SYSTEM
}

/** App-wide preferences (not per-device). */
data class ConnectionSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)
