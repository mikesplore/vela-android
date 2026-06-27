package com.template.app.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssistantRequest(
    val message: String
)

@JsonClass(generateAdapter = true)
data class AssistantResponse(
    val reply: String,
    val thinking: String? = null,
    @Json(name = "tool_calls") val toolCalls: List<ToolCallDto>? = null,
    @Json(name = "image_base64") val imageBase64: String? = null,
    @Json(name = "art_url") val artUrl: String? = null,
    @Json(name = "pending_action_id") val pendingActionId: String? = null,
    @Json(name = "requires_confirmation") val requiresConfirmation: Boolean = false,
    @Json(name = "requires_auth") val requiresAuth: Boolean = false,
    @Json(name = "expires_in_seconds") val expiresInSeconds: Int? = null,
    val confirmation: ConfirmationCard? = null
)

@JsonClass(generateAdapter = true)
data class ToolCallDto(
    val name: String,
    val status: String,
    val result: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class ConfirmationCard(
    val title: String,
    val description: String,
    @Json(name = "action_type") val actionType: String,
    @Json(name = "tool_count") val toolCount: Int,
    @Json(name = "requires_auth") val requiresAuth: Boolean,
    @Json(name = "action_details") val actionDetails: List<String>,
    @Json(name = "prompt_text") val promptText: String,
    @Json(name = "pin_attempts_remaining") val pinAttemptsRemaining: Int? = null,
    @Json(name = "pin_max_attempts") val pinMaxAttempts: Int? = null
)
