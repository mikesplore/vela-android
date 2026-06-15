package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.*

@Entity(tableName = "vela_health")
data class VelaHealthEntity(
    @PrimaryKey val id: Int = 0,
    val status: String,
    val uptimeSeconds: Long
) {
    fun toDomain() = VelaHealth(status, uptimeSeconds)
    companion object {
        fun fromDomain(domain: VelaHealth) = VelaHealthEntity(status = domain.status, uptimeSeconds = domain.uptimeSeconds)
    }
}

@Entity(tableName = "vela_network")
data class VelaNetworkEntity(
    @PrimaryKey val id: Int = 0,
    val localIp: String,
    val publicIp: String,
    val interfaceName: String
) {
    fun toDomain() = VelaNetworkInfo(localIp, publicIp, interfaceName)
    companion object {
        fun fromDomain(domain: VelaNetworkInfo) = VelaNetworkEntity(
            localIp = domain.localIp,
            publicIp = domain.publicIp,
            interfaceName = domain.interfaceName
        )
    }
}

@Entity(tableName = "vela_audio")
data class VelaAudioEntity(
    @PrimaryKey val id: Int = 0,
    val volume: Int,
    val muted: Boolean
) {
    fun toDomain() = VelaAudioState(volume, muted)
    companion object {
        fun fromDomain(domain: VelaAudioState) = VelaAudioEntity(volume = domain.volume, muted = domain.muted)
    }
}

@Entity(tableName = "vela_media")
data class VelaMediaEntity(
    @PrimaryKey val id: Int = 0,
    val title: String?,
    val artist: String?,
    val album: String?,
    val status: String?,
    val positionSeconds: Double?,
    val lengthSeconds: Double?
) {
    fun toDomain() = VelaMediaState(title, artist, album, status, positionSeconds, lengthSeconds)
    companion object {
        fun fromDomain(domain: VelaMediaState) = VelaMediaEntity(
            title = domain.title,
            artist = domain.artist,
            album = domain.album,
            status = domain.status,
            positionSeconds = domain.positionSeconds,
            lengthSeconds = domain.lengthSeconds
        )
    }
}

@Entity(tableName = "vela_processes")
data class VelaProcessEntity(
    @PrimaryKey val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double
) {
    fun toDomain() = VelaProcess(pid, name, cpu, mem)
    companion object {
        fun fromDomain(domain: VelaProcess) = VelaProcessEntity(
            pid = domain.pid,
            name = domain.name,
            cpu = domain.cpu,
            mem = domain.mem
        )
    }
}

@Entity(tableName = "vela_disks")
data class VelaDiskEntity(
    @PrimaryKey val mountpoint: String,
    val total: Long,
    val used: Long,
    val free: Long,
    val percent: Double
) {
    fun toDomain() = VelaDiskUsage(mountpoint, total, used, free, percent)
    companion object {
        fun fromDomain(domain: VelaDiskUsage) = VelaDiskEntity(
            mountpoint = domain.mountpoint,
            total = domain.total,
            used = domain.used,
            free = domain.free,
            percent = domain.percent
        )
    }
}
