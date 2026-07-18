package com.template.app.core.data.repository

import com.template.app.core.data.local.AppDatabase
import com.template.app.core.data.local.UserPreferencesDataStore
import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.dao.PairedDeviceDao
import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.PairedDeviceEntity
import com.template.app.domain.model.PairedDevice
import com.template.app.domain.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val pairedDeviceDao: PairedDeviceDao,
    private val velaDao: VelaDao,
    private val assistantDao: AssistantDao,
    private val database: AppDatabase,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : DeviceRepository {

    override fun observeDevices(): Flow<List<PairedDevice>> =
        pairedDeviceDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActiveDevice(): Flow<PairedDevice?> =
        pairedDeviceDao.observeActive().map { it?.toDomain() }

    override suspend fun getActiveDevice(): PairedDevice? =
        pairedDeviceDao.getActive()?.toDomain()

    override suspend fun getDevice(id: Long): PairedDevice? =
        pairedDeviceDao.getById(id)?.toDomain()

    override suspend fun hasDevices(): Boolean =
        pairedDeviceDao.count() > 0

    override suspend fun addOrUpdateDevice(
        agentId: String,
        relayBaseUrl: String,
        relaySecret: String,
        label: String,
        username: String?,
        hostname: String?,
        vpsUrl: String?
    ): PairedDevice = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val trimmedUrl = relayBaseUrl.trim()
        val trimmedSecret = relaySecret.trim()

        val existing = pairedDeviceDao.getByAgentId(agentId)
            ?: pairedDeviceDao.getByRelay(trimmedUrl, trimmedSecret)

        val entity = if (existing != null) {
            existing.copy(
                agentId = agentId,
                relayBaseUrl = trimmedUrl,
                relaySecret = trimmedSecret,
                label = label.ifBlank { existing.label },
                username = username ?: existing.username,
                hostname = hostname ?: existing.hostname,
                vpsUrl = vpsUrl ?: existing.vpsUrl,
                lastUsedAt = now,
                isActive = true
            ).also {
                pairedDeviceDao.clearActiveFlags()
                pairedDeviceDao.update(it.copy(isActive = true))
            }
        } else {
            pairedDeviceDao.clearActiveFlags()
            val toInsert = PairedDeviceEntity(
                agentId = agentId,
                relayBaseUrl = trimmedUrl,
                relaySecret = trimmedSecret,
                label = label.ifBlank { "Device" },
                hostname = hostname,
                username = username,
                vpsUrl = vpsUrl,
                pairedAt = now,
                lastUsedAt = now,
                isActive = true
            )
            val id = pairedDeviceDao.insert(toInsert)
            toInsert.copy(id = id)
        }

        // Re-read to return canonical row
        pairedDeviceDao.getActive()?.toDomain()
            ?: entity.toDomain()
    }

    override suspend fun switchDevice(id: Long) = withContext(Dispatchers.IO) {
        require(pairedDeviceDao.getById(id) != null) { "Device $id not found" }
        pairedDeviceDao.activate(id)
    }

    override suspend fun renameDevice(id: Long, label: String) = withContext(Dispatchers.IO) {
        pairedDeviceDao.rename(id, label.trim())
    }

    override suspend fun updateDeviceMetadata(
        id: Long,
        hostname: String?,
        username: String?
    ) = withContext(Dispatchers.IO) {
        pairedDeviceDao.updateMetadata(id, hostname, username)
    }

    override suspend fun removeDevice(id: Long): Boolean = withContext(Dispatchers.IO) {
        val removing = pairedDeviceDao.getById(id) ?: return@withContext false
        velaDao.deleteAllForConnection(id)
        assistantDao.clearChat(id)
        pairedDeviceDao.deleteById(id)

        if (removing.isActive) {
            val next = pairedDeviceDao.getAll().maxByOrNull { it.lastUsedAt }
            if (next != null) {
                pairedDeviceDao.activate(next.id)
            }
        }
        true
    }

    override suspend fun removeAllDevices() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        userPreferencesDataStore.clearAll()
    }
}
