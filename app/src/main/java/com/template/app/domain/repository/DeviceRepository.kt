package com.template.app.domain.repository

import com.template.app.domain.model.PairedDevice
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<PairedDevice>>
    fun observeActiveDevice(): Flow<PairedDevice?>
    suspend fun getActiveDevice(): PairedDevice?
    suspend fun getDevice(id: Long): PairedDevice?
    suspend fun hasDevices(): Boolean

    /**
     * Persist a newly paired agent (or update/dedupe by agentId / relay credentials)
     * and make it the active device.
     */
    suspend fun addOrUpdateDevice(
        agentId: String,
        relayBaseUrl: String,
        relaySecret: String,
        label: String,
        username: String? = null,
        hostname: String? = null,
        vpsUrl: String? = null
    ): PairedDevice

    suspend fun switchDevice(id: Long)
    suspend fun renameDevice(id: Long, label: String)
    suspend fun updateDeviceMetadata(id: Long, hostname: String?, username: String?)
    suspend fun removeDevice(id: Long): Boolean
    suspend fun removeAllDevices()
}
