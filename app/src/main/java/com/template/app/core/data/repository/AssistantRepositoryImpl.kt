package com.template.app.core.data.repository

import android.content.Context
import android.util.Base64
import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.entities.AssistantMessageEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.network.NetworkErrors
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.AssistantRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

        var connectionId: Long? = null
        var currentAssistantMsg: AssistantChatMessage? = null

        try {
            connectionId = activeConnection.requireActiveId()

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

            // Assistant placeholder — filled by stream events, or error text on failure
            currentAssistantMsg = AssistantChatMessage(
                id = UUID.randomUUID().toString(),
                text = "",
                isUser = false,
                isStreaming = true
            )
            assistantDao.upsertMessage(
                AssistantMessageEntity.fromDomain(connectionId, currentAssistantMsg, moshi)
            )

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
                                    val jsonToParse = if (data == "{}") {
                                        "{\"type\":\"$currentEvent\"}"
                                    } else {
                                        data.replaceFirst("{", "{\"type\":\"$currentEvent\",")
                                    }
                                    eventAdapter.fromJson(jsonToParse)
                                } catch (_: Exception) {
                                    null
                                }

                                if (event != null) {
                                    var nextImageBase64: String? = null
                                    var msg = currentAssistantMsg!!

                                    msg = when (event) {
                                        is VelaStreamEvent.Thinking -> {
                                            msg.copy(thinkingText = (msg.thinkingText ?: "") + event.text)
                                        }
                                        is VelaStreamEvent.ToolExecution -> {
                                            val newToolCalls = msg.toolCalls.toMutableList()
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

                                            var nextArtUrl = event.artUrl ?: msg.artUrl
                                            nextImageBase64 = event.imageBase64

                                            event.result?.let { result ->
                                                result["art_url"]?.toString()?.let { nextArtUrl = it }
                                                result["image_base64"]?.toString()?.let { nextImageBase64 = it }
                                            }

                                            msg.copy(
                                                toolCalls = newToolCalls,
                                                artUrl = nextArtUrl
                                            )
                                        }
                                        is VelaStreamEvent.Content -> {
                                            nextImageBase64 = event.imageBase64
                                            msg.copy(
                                                text = msg.text + event.text,
                                                artUrl = event.artUrl ?: msg.artUrl
                                            )
                                        }
                                        is VelaStreamEvent.Gate -> {
                                            nextImageBase64 = event.imageBase64
                                            msg.copy(
                                                pendingActionId = event.pendingActionId,
                                                isPinRequired = event.requiresAuth,
                                                confirmation = event.confirmation?.copy(
                                                    expiresInSeconds = event.expiresInSeconds ?: event.confirmation.expiresInSeconds,
                                                    requiresAuth = event.requiresAuth || event.confirmation.requiresAuth
                                                ),
                                                artUrl = event.artUrl ?: msg.artUrl
                                            )
                                        }
                                        is VelaStreamEvent.Screenshot -> {
                                            nextImageBase64 = event.imageBase64
                                            msg
                                        }
                                        is VelaStreamEvent.Error -> {
                                            val errorText = event.text.ifBlank {
                                                NetworkErrors.GENERIC_ERROR
                                            }
                                            val reply = if (msg.text.isBlank()) {
                                                "Sorry — I couldn't complete that request.\n\n$errorText"
                                            } else {
                                                "${msg.text}\n\nRequest failed: $errorText"
                                            }
                                            msg.copy(text = reply, isStreaming = false)
                                        }
                                        is VelaStreamEvent.Done -> {
                                            nextImageBase64 = event.imageBase64
                                            msg.copy(
                                                isStreaming = false,
                                                artUrl = event.artUrl ?: msg.artUrl
                                            )
                                        }
                                    }

                                    if (nextImageBase64 != null) {
                                        val path = saveBase64ToFile(nextImageBase64, msg.id)
                                        msg = msg.copy(imagePath = path)
                                    }

                                    currentAssistantMsg = msg
                                    assistantDao.upsertMessage(
                                        AssistantMessageEntity.fromDomain(connectionId, msg, moshi)
                                    )
                                }
                            }
                        }
                        line.isBlank() -> {
                            currentEvent = null
                        }
                    }
                }
            }

            // Stream ended without a Done event — finalize so the bubble is not stuck streaming
            currentAssistantMsg?.let { msg ->
                if (msg.isStreaming) {
                    val finalized = msg.copy(isStreaming = false)
                    currentAssistantMsg = finalized
                    assistantDao.upsertMessage(
                        AssistantMessageEntity.fromDomain(connectionId, finalized, moshi)
                    )
                }
            }

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            val errorMessage = mapStreamError(e)
            persistAssistantError(connectionId, currentAssistantMsg, errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.flowOn(Dispatchers.IO)

    private fun mapStreamError(e: Exception): String = when (e) {
        is HttpException -> NetworkErrors.getMessageForCode(e.code())
        is IOException -> NetworkErrors.NETWORK_ERROR
        else -> e.localizedMessage?.takeIf { it.isNotBlank() } ?: NetworkErrors.GENERIC_ERROR
    }

    private suspend fun persistAssistantError(
        connectionId: Long?,
        currentAssistantMsg: AssistantChatMessage?,
        errorMessage: String
    ) {
        val id = connectionId ?: return
        val replyText = if (currentAssistantMsg == null || currentAssistantMsg.text.isBlank()) {
            "Sorry — I couldn't complete that request.\n\n$errorMessage"
        } else {
            "${currentAssistantMsg.text}\n\nRequest failed: $errorMessage"
        }
        val errorMsg = (currentAssistantMsg ?: AssistantChatMessage(
            text = "",
            isUser = false
        )).copy(
            text = replyText,
            isStreaming = false
        )
        assistantDao.upsertMessage(AssistantMessageEntity.fromDomain(id, errorMsg, moshi))
    }

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
