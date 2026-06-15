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
        fun fromDomain(domain: VelaHealth) = VelaHealthEntity(id = 0, status = domain.status, uptimeSeconds = domain.uptimeSeconds)
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
            id = 0,
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
        fun fromDomain(domain: VelaAudioState) = VelaAudioEntity(id = 0, volume = domain.volume, muted = domain.muted)
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
            id = 0,
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

@Entity(tableName = "vela_notifications")
data class VelaNotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val appName: String?,
    val timestamp: Long
) {
    fun toDomain() = VelaNotification(id, title, message, appName, timestamp)
    companion object {
        fun fromDomain(domain: VelaNotification) = VelaNotificationEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            appName = domain.appName,
            timestamp = domain.timestamp
        )
    }
}

@Entity(tableName = "vela_wifi")
data class VelaWifiEntity(
    @PrimaryKey val id: Int = 0,
    val connected: Boolean,
    val ssid: String?,
    val signal: Int?
) {
    fun toDomain() = VelaWifiStatus(connected, ssid, signal)
    companion object {
        fun fromDomain(domain: VelaWifiStatus) = VelaWifiEntity(
            id = 0,
            connected = domain.connected,
            ssid = domain.ssid,
            signal = domain.signal
        )
    }
}

@Entity(tableName = "vela_brightness")
data class VelaBrightnessEntity(
    @PrimaryKey val id: Int = 0,
    val value: Int
) {
    fun toDomain() = VelaBrightness(value)
    companion object {
        fun fromDomain(domain: VelaBrightness) = VelaBrightnessEntity(id = 0, value = domain.value)
    }
}

@Entity(tableName = "vela_resolution")
data class VelaResolutionEntity(
    @PrimaryKey val id: Int = 0,
    val width: Int,
    val height: Int,
    val refresh: Double,
    val output: String?
) {
    fun toDomain() = VelaResolution(width, height, refresh, output)
    companion object {
        fun fromDomain(domain: VelaResolution) = VelaResolutionEntity(
            id = 0,
            width = domain.width,
            height = domain.height,
            refresh = domain.refresh,
            output = domain.output
        )
    }
}
