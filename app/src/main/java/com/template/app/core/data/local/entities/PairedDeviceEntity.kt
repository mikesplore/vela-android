package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.PairedDevice

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agentId: String,
    val relayBaseUrl: String,
    val relaySecret: String,
    val label: String,
    val hostname: String? = null,
    val username: String? = null,
    val vpsUrl: String? = null,
    val pairedAt: Long,
    val lastUsedAt: Long,
    val isActive: Boolean
) {
    fun toDomain() = PairedDevice(
        id = id,
        agentId = agentId,
        relayBaseUrl = relayBaseUrl,
        relaySecret = relaySecret,
        label = label,
        hostname = hostname,
        username = username,
        vpsUrl = vpsUrl,
        pairedAt = pairedAt,
        lastUsedAt = lastUsedAt,
        isActive = isActive
    )

    companion object {
        fun fromDomain(domain: PairedDevice) = PairedDeviceEntity(
            id = domain.id,
            agentId = domain.agentId,
            relayBaseUrl = domain.relayBaseUrl.trim(),
            relaySecret = domain.relaySecret.trim(),
            label = domain.label,
            hostname = domain.hostname,
            username = domain.username,
            vpsUrl = domain.vpsUrl,
            pairedAt = domain.pairedAt,
            lastUsedAt = domain.lastUsedAt,
            isActive = domain.isActive
        )
    }
}
