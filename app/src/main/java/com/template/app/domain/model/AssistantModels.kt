package com.template.app.domain.model

import com.squareup.moshi.JsonClass
import java.util.UUID

sealed class VelaStreamEvent {
    @JsonClass(generateAdapter = true)
    data class Thinking(val text: String) : VelaStreamEvent()
    @JsonClass(generateAdapter = true)
    data class ToolExecution(val name: String, val status: String, val result: Map<String, Any>? = null) : VelaStreamEvent()
    @JsonClass(generateAdapter = true)
    data class Content(val text: String) : VelaStreamEvent()
    @JsonClass(generateAdapter = true)
    class Done : VelaStreamEvent()
}

data class AssistantChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val artUrl: String? = null,
    val confirmation: AssistantConfirmation? = null,
    val isPinRequired: Boolean = false,
    val pendingActionId: String? = null,
    val thinkingText: String? = null,
    val toolCalls: List<ToolCall> = emptyList(),
    val isStreaming: Boolean = false
)

data class ToolCall(
    val name: String,
    val status: String,
    val result: String? = null
)

data class AssistantConfirmation(
    val title: String,
    val description: String,
    val actionType: String,
    val details: List<String>,
    val promptText: String,
    val expiresInSeconds: Int? = null,
    val requiresAuth: Boolean = false
)
