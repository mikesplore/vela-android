package com.template.app.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

sealed class VelaStreamEvent {
    @JsonClass(generateAdapter = true)
    data class Thinking(val text: String) : VelaStreamEvent()
    
    @JsonClass(generateAdapter = true)
    data class ToolExecution(
        val name: String,
        val status: String,
        val result: Map<String, Any>? = null,
        @Json(name = "image_base64") val imageBase64: String? = null,
        @Json(name = "art_url") val artUrl: String? = null
    ) : VelaStreamEvent()
    
    @JsonClass(generateAdapter = true)
    data class Content(
        val text: String,
        @Json(name = "image_base64") val imageBase64: String? = null,
        @Json(name = "art_url") val artUrl: String? = null
    ) : VelaStreamEvent()
    
    @JsonClass(generateAdapter = true)
    data class Gate(
        @Json(name = "pending_action_id") val pendingActionId: String?,
        @Json(name = "requires_confirmation") val requiresConfirmation: Boolean = false,
        @Json(name = "requires_auth") val requiresAuth: Boolean = false,
        @Json(name = "expires_in_seconds") val expiresInSeconds: Int? = null,
        val confirmation: AssistantConfirmation? = null,
        @Json(name = "image_base64") val imageBase64: String? = null,
        @Json(name = "art_url") val artUrl: String? = null
    ) : VelaStreamEvent()

    @JsonClass(generateAdapter = true)
    data class Screenshot(
        @Json(name = "image_base64") val imageBase64: String? = null
    ) : VelaStreamEvent()

    @JsonClass(generateAdapter = true)
    data class Error(
        val text: String
    ) : VelaStreamEvent()
    
    @JsonClass(generateAdapter = true)
    data class Done(
        @Json(name = "image_base64") val imageBase64: String? = null,
        @Json(name = "art_url") val artUrl: String? = null
    ) : VelaStreamEvent()
}

enum class SecureReplyKind {
    CONFIRMED,
    CANCELLED,
    PIN_VERIFIED
}

/** Local send lifecycle for chat — not persisted. */
enum class AssistantSendPhase {
    Idle,
    Preparing,
    Connecting,
    Streaming
}

data class AssistantChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null, // Changed from imageBase64
    val artUrl: String? = null,
    val confirmation: AssistantConfirmation? = null,
    val isPinRequired: Boolean = false,
    val pendingActionId: String? = null,
    val thinkingText: String? = null,
    val toolCalls: List<ToolCall> = emptyList(),
    val isStreaming: Boolean = false,
    val secureReplyKind: SecureReplyKind? = null
)

data class ToolCall(
    val name: String,
    val status: String,
    val result: String? = null
)

@JsonClass(generateAdapter = true)
data class AssistantConfirmation(
    val title: String,
    val description: String,
    @Json(name = "action_type") val actionType: String,
    @Json(name = "action_details") val details: List<String>,
    @Json(name = "prompt_text") val promptText: String,
    @Json(name = "expires_in_seconds") val expiresInSeconds: Int? = null,
    @Json(name = "requires_auth") val requiresAuth: Boolean = false
)
