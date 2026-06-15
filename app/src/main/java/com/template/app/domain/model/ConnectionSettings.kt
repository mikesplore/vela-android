package com.template.app.domain.model

data class ConnectionSettings(
    val baseUrl: String = "",
    val apiToken: String = "",
    val onboardingComplete: Boolean = false
)
