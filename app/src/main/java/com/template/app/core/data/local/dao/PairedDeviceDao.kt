package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.template.app.core.data.local.entities.PairedDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedDeviceDao {

    @Query("SELECT * FROM paired_devices ORDER BY lastUsedAt DESC")
    fun observeAll(): Flow<List<PairedDeviceEntity>>

    @Query("SELECT * FROM paired_devices ORDER BY lastUsedAt DESC")
    suspend fun getAll(): List<PairedDeviceEntity>

    @Query("SELECT * FROM paired_devices WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<PairedDeviceEntity?>

    @Query("SELECT * FROM paired_devices WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): PairedDeviceEntity?

    @Query("SELECT * FROM paired_devices WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PairedDeviceEntity?

    @Query("SELECT * FROM paired_devices WHERE agentId = :agentId LIMIT 1")
    suspend fun getByAgentId(agentId: String): PairedDeviceEntity?

    @Query("SELECT * FROM paired_devices WHERE relayBaseUrl = :url AND relaySecret = :secret LIMIT 1")
    suspend fun getByRelay(url: String, secret: String): PairedDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: PairedDeviceEntity): Long

    @Update
    suspend fun update(device: PairedDeviceEntity)

    @Query("UPDATE paired_devices SET isActive = 0")
    suspend fun clearActiveFlags()

    @Query("UPDATE paired_devices SET isActive = 1, lastUsedAt = :lastUsedAt WHERE id = :id")
    suspend fun setActive(id: Long, lastUsedAt: Long = System.currentTimeMillis())

    @Query("UPDATE paired_devices SET label = :label WHERE id = :id")
    suspend fun rename(id: Long, label: String)

    @Query("UPDATE paired_devices SET hostname = :hostname, username = :username WHERE id = :id")
    suspend fun updateMetadata(id: Long, hostname: String?, username: String?)

    @Query("DELETE FROM paired_devices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM paired_devices")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM paired_devices")
    suspend fun count(): Int

    @Transaction
    suspend fun activate(id: Long) {
        clearActiveFlags()
        setActive(id)
    }
}
