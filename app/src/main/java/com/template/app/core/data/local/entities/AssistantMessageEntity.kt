package com.template.app.core.data.local.entities

import androidx.room.Entity
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.AssistantConfirmation
import com.template.app.domain.model.ToolCall
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(
    tableName = "assistant_messages",
    primaryKeys = ["connectionId", "id"]
)
data class AssistantMessageEntity(
    val connectionId: Long,
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imagePath: String?,
    val artUrl: String?,
    val isPinRequired: Boolean,
    val pendingActionId: String?,
    val thinkingText: String?,
    val toolCallsJson: String?,
    val confTitle: String?,
    val confDescription: String?,
    val confActionType: String?,
    val confDetails: List<String>?,
    val confPromptText: String?,
    val confExpiresInSeconds: Int?,
    val confRequiresAuth: Boolean?
) {
    fun toDomain(moshi: Moshi): AssistantChatMessage {
        val confirmation = if (confTitle != null) {
            AssistantConfirmation(
                title = confTitle,
                description = confDescription ?: "",
                actionType = confActionType ?: "",
                details = confDetails ?: emptyList(),
                promptText = confPromptText ?: "",
                expiresInSeconds = confExpiresInSeconds,
                requiresAuth = confRequiresAuth ?: false
            )
        } else null

        val toolCalls = toolCallsJson?.let {
            val type = Types.newParameterizedType(List::class.java, ToolCall::class.java)
            moshi.adapter<List<ToolCall>>(type).fromJson(it)
        } ?: emptyList()

        return AssistantChatMessage(
            id = id,
            text = text,
            isUser = isUser,
            timestamp = timestamp,
            imagePath = imagePath,
            artUrl = artUrl,
            confirmation = confirmation,
            isPinRequired = isPinRequired,
            pendingActionId = pendingActionId,
            thinkingText = thinkingText,
            toolCalls = toolCalls,
            isStreaming = false
        )
    }

    companion object {
        fun fromDomain(
            connectionId: Long,
            domain: AssistantChatMessage,
            moshi: Moshi
        ): AssistantMessageEntity {
            val type = Types.newParameterizedType(List::class.java, ToolCall::class.java)
            val toolCallsJson = moshi.adapter<List<ToolCall>>(type).toJson(domain.toolCalls)

            return AssistantMessageEntity(
                connectionId = connectionId,
                id = domain.id,
                text = domain.text,
                isUser = domain.isUser,
                timestamp = domain.timestamp,
                imagePath = domain.imagePath,
                artUrl = domain.artUrl,
                isPinRequired = domain.isPinRequired,
                pendingActionId = domain.pendingActionId,
                thinkingText = domain.thinkingText,
                toolCallsJson = toolCallsJson,
                confTitle = domain.confirmation?.title,
                confDescription = domain.confirmation?.description,
                confActionType = domain.confirmation?.actionType,
                confDetails = domain.confirmation?.details,
                confPromptText = domain.confirmation?.promptText,
                confExpiresInSeconds = domain.confirmation?.expiresInSeconds,
                confRequiresAuth = domain.confirmation?.requiresAuth
            )
        }
    }
}
