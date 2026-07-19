package com.template.app.core.data.repository

import android.content.Context
import android.util.Base64
import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.entities.AssistantMessageEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.AssistantRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val assistantDao: AssistantDao,
    private val moshi: Moshi,
    private val activeConnection: ActiveConnectionProvider,
    @ApplicationContext private val context: Context
) : AssistantRepository {

    private val sessionId = UUID.randomUUID().toString()
    private val eventAdapter = moshi.adapter(VelaStreamEvent::class.java)

    override fun observeMessages(): Flow<List<AssistantChatMessage>> =
        activeConnection.scoped(emptyList()) { id ->
            assistantDao.observeMessages(id).map { entities ->
                entities.map { it.toDomain(moshi) }
            }
        }

    override suspend fun sendMessage(message: String): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val userMsg = AssistantChatMessage(text = message, isUser = true)
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(connectionId, userMsg, moshi))

        val response = apiService.assistantChat(
            sessionId = sessionId,
            body = AssistantRequest(message = message)
        )

        val assistantMsg = response.toDomain()
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(connectionId, assistantMsg, moshi))
        Unit
    }

    override fun sendMessageStream(
        message: String,
        secureReplyKind: SecureReplyKind?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)

        val connectionId = activeConnection.requireActiveId()

        // Persist display-safe text for secure replies (never store PIN digits in Room)
        val displayText = when (secureReplyKind) {
            SecureReplyKind.PIN_VERIFIED -> ""
            SecureReplyKind.CONFIRMED, SecureReplyKind.CANCELLED -> ""
            null -> message
        }
        val userMsg = AssistantChatMessage(
            text = displayText,
            isUser = true,
            secureReplyKind = secureReplyKind
        )
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(connectionId, userMsg, moshi))

        // 2. Prepare assistant message placeholder
        val assistantMsgId = UUID.randomUUID().toString()
        var currentAssistantMsg = AssistantChatMessage(
            id = assistantMsgId,
            text = "",
            isUser = false,
            isStreaming = true
        )
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(connectionId, currentAssistantMsg, moshi))

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
                                    var nextImageBase64: String? = null

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
                                                result = event.result?.let { moshi.adapter(Map::class.java).toJson(it) }
                                            )
                                            if (existingIndex >= 0) {
                                                newToolCalls[existingIndex] = updatedToolCall
                                            } else {
                                                newToolCalls.add(updatedToolCall)
                                            }

                                            // Extract art_url or image_base64 from event fields or nested result
                                            var nextArtUrl = event.artUrl ?: currentAssistantMsg.artUrl
                                            nextImageBase64 = event.imageBase64

                                            event.result?.let { result ->
                                                result["art_url"]?.toString()?.let { nextArtUrl = it }
                                                result["image_base64"]?.toString()?.let { nextImageBase64 = it }
                                            }

                                            currentAssistantMsg.copy(
                                                toolCalls = newToolCalls,
                                                artUrl = nextArtUrl
                                            )
                                        }
                                        is VelaStreamEvent.Content -> {
                                            nextImageBase64 = event.imageBase64
                                            currentAssistantMsg.copy(
                                                text = currentAssistantMsg.text + event.text,
                                                artUrl = event.artUrl ?: currentAssistantMsg.artUrl
                                            )
                                        }
                                        is VelaStreamEvent.Gate -> {
                                            nextImageBase64 = event.imageBase64
                                            currentAssistantMsg.copy(
                                                pendingActionId = event.pendingActionId,
                                                isPinRequired = event.requiresAuth,
                                                confirmation = event.confirmation?.copy(
                                                    expiresInSeconds = event.expiresInSeconds ?: event.confirmation.expiresInSeconds,
                                                    requiresAuth = event.requiresAuth || event.confirmation.requiresAuth
                                                ),
                                                artUrl = event.artUrl ?: currentAssistantMsg.artUrl
                                            )
                                        }
                                        is VelaStreamEvent.Screenshot -> {
                                            nextImageBase64 = event.imageBase64
                                            currentAssistantMsg
                                        }
                                        is VelaStreamEvent.Done -> {
                                            nextImageBase64 = event.imageBase64
                                            currentAssistantMsg.copy(
                                                isStreaming = false,
                                                artUrl = event.artUrl ?: currentAssistantMsg.artUrl
                                            )
                                        }
                                    }

                                    if (nextImageBase64 != null) {
                                        val path = saveBase64ToFile(nextImageBase64, currentAssistantMsg.id)
                                        currentAssistantMsg = currentAssistantMsg.copy(imagePath = path)
                                    }

                                    assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(connectionId, currentAssistantMsg, moshi))
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
        val connectionId = activeConnection.requireActiveId()
        assistantDao.clearChat(connectionId)
        // Also clear saved images
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "assistant_images")
            if (dir.exists()) dir.deleteRecursively()
        }
    }

    private suspend fun AssistantResponse.toDomain(): AssistantChatMessage {
        val path = imageBase64?.let { saveBase64ToFile(it, UUID.randomUUID().toString()) }
        return AssistantChatMessage(
            text = reply,
            isUser = false,
            imagePath = path,
            artUrl = artUrl,
            confirmation = confirmation?.toDomain(expiresInSeconds),
            isPinRequired = requiresAuth,
            pendingActionId = pendingActionId,
            thinkingText = thinking,
            toolCalls = toolCalls?.map { it.toDomain() } ?: emptyList()
        )
    }

    private suspend fun saveBase64ToFile(base64: String, messageId: String): String? = withContext(Dispatchers.IO) {
        try {
            val raw = if (base64.contains(",")) base64.substringAfter(",") else base64
            val bytes = Base64.decode(raw, Base64.DEFAULT)

            val dir = File(context.filesDir, "assistant_images")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "img_$messageId.png")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun ToolCallDto.toDomain(): ToolCall {
        return ToolCall(
            name = name,
            status = status,
            result = result?.let { moshi.adapter(Map::class.java).toJson(it) }
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
