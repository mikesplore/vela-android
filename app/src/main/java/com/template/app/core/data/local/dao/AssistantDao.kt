package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.template.app.core.data.local.entities.AssistantMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    @Query("SELECT * FROM assistant_messages WHERE connectionId = :connectionId ORDER BY timestamp ASC")
    fun observeMessages(connectionId: Long): Flow<List<AssistantMessageEntity>>

    @Upsert
    suspend fun upsertMessage(message: AssistantMessageEntity)

    @Upsert
    suspend fun upsertMessages(messages: List<AssistantMessageEntity>)

    @Query("DELETE FROM assistant_messages WHERE connectionId = :connectionId")
    suspend fun clearChat(connectionId: Long)
}
