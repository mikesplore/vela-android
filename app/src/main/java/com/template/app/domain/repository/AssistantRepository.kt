package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.AssistantSendEvent
import com.template.app.domain.model.SecureReplyKind
import kotlinx.coroutines.flow.Flow

interface AssistantRepository {
    fun observeMessages(): Flow<List<AssistantChatMessage>>
    suspend fun sendMessage(message: String): Resource<Unit>
    fun sendMessageStream(
        message: String,
        secureReplyKind: SecureReplyKind? = null
    ): Flow<AssistantSendEvent>
    suspend fun clearChat()
}
