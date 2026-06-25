package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.entities.AssistantMessageEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.AssistantRequest
import com.template.app.core.data.remote.dto.AssistantResponse
import com.template.app.core.data.remote.dto.ConfirmationCard
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.AssistantRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val assistantDao: AssistantDao,
    private val moshi: Moshi
) : AssistantRepository {

    private val sessionId = UUID.randomUUID().toString()
    private val eventAdapter = moshi.adapter(VelaStreamEvent::class.java)

    override fun observeMessages(): Flow<List<AssistantChatMessage>> = 
        assistantDao.observeMessages().map { entities ->
            entities.map { it.toDomain(moshi) }
        }

    override suspend fun sendMessage(message: String): Resource<Unit> = safeApiCall {
        val userMsg = AssistantChatMessage(text = message, isUser = true)
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(userMsg, moshi))

        val response = apiService.assistantChat(
            sessionId = sessionId,
            body = AssistantRequest(message = message)
        )

        val assistantMsg = response.toDomain()
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(assistantMsg, moshi))
        Unit
    }

    override fun sendMessageStream(message: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        
        // 1. Save user message
        val userMsg = AssistantChatMessage(text = message, isUser = true)
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(userMsg, moshi))

        // 2. Prepare assistant message placeholder
        val assistantMsgId = UUID.randomUUID().toString()
        var currentAssistantMsg = AssistantChatMessage(
            id = assistantMsgId,
            text = "",
            isUser = false,
            isStreaming = true
        )
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(currentAssistantMsg, moshi))

        try {
            val responseBody = apiService.assistantStream(
                sessionId = sessionId,
                body = AssistantRequest(message = message)
            )

            responseBody.source().use { source ->
                var currentEvent: String? = null
                
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    
                    when {
                        line.startsWith("event:") -> {
                            currentEvent = line.substringAfter("event:").trim()
                        }
                        line.startsWith("data:") -> {
                            val data = line.substringAfter("data:").trim()
                            if (currentEvent != null && data.isNotBlank()) {
                                val event = try {
                                    // Construct the JSON with type field for the polymorphic adapter
                                    val jsonToParse = if (data == "{}") {
                                        "{\"type\":\"$currentEvent\"}"
                                    } else {
                                        data.replaceFirst("{", "{\"type\":\"$currentEvent\",")
                                    }
                                    eventAdapter.fromJson(jsonToParse)
                                } catch (e: Exception) {
                                    null
                                }

                                if (event != null) {
                                    currentAssistantMsg = when (event) {
                                        is VelaStreamEvent.Thinking -> {
                                            currentAssistantMsg.copy(thinkingText = (currentAssistantMsg.thinkingText ?: "") + event.text)
                                        }
                                        is VelaStreamEvent.ToolExecution -> {
                                            val newToolCalls = currentAssistantMsg.toolCalls.toMutableList()
                                            val existingIndex = newToolCalls.indexOfFirst { it.name == event.name }
                                            val updatedToolCall = ToolCall(
                                                name = event.name,
                                                status = event.status,
                                                result = event.result?.toString()
                                            )
                                            if (existingIndex >= 0) {
                                                newToolCalls[existingIndex] = updatedToolCall
                                            } else {
                                                newToolCalls.add(updatedToolCall)
                                            }
                                            currentAssistantMsg.copy(toolCalls = newToolCalls)
                                        }
                                        is VelaStreamEvent.Content -> {
                                            currentAssistantMsg.copy(text = currentAssistantMsg.text + event.text)
                                        }
                                        is VelaStreamEvent.Done -> {
                                            currentAssistantMsg.copy(isStreaming = false)
                                        }
                                    }
                                    assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(currentAssistantMsg, moshi))
                                }
                            }
                        }
                        line.isBlank() -> {
                            currentEvent = null
                        }
                    }
                }
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun clearChat() {
        assistantDao.clearChat()
    }

    private fun AssistantResponse.toDomain(): AssistantChatMessage {
        return AssistantChatMessage(
            text = reply,
            isUser = false,
            imageBase64 = imageBase64,
            artUrl = artUrl,
            confirmation = confirmation?.toDomain(expiresInSeconds),
            isPinRequired = requiresAuth,
            pendingActionId = pendingActionId
        )
    }

    private fun ConfirmationCard.toDomain(expiresInSeconds: Int?): AssistantConfirmation {
        return AssistantConfirmation(
            title = title,
            description = description,
            actionType = actionType,
            details = actionDetails,
            promptText = promptText,
            expiresInSeconds = expiresInSeconds,
            requiresAuth = requiresAuth
        )
    }
}
