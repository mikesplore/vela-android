package com.template.app.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.template.app.core.data.local.entities.UserEntity

/**
 * Data Transfer Object — mirrors the JSON shape from the API.
 * Never expose DTOs outside the data layer; map to domain models instead.
 */
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id")          val id: String,
    @Json(name = "displayName") val displayName: String?,
    @Json(name = "email")        val email: String,
    @Json(name = "avatarUrl")   val avatarUrl: String?
) {
    /** Maps network DTO → Room entity for caching */
    fun toEntity() = UserEntity(
        id = id,
        displayName = displayName ?: "Unknown User",
        email = email,
        avatarUrl = avatarUrl
    )
}
