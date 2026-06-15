package com.template.app.domain.model

/**
 * Domain model — pure Kotlin data class with zero framework dependencies.
 * This is what the UI layer sees; never Room entities or Retrofit DTOs.
 */
data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?
)
