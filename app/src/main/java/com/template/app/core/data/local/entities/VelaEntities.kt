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
    val publicIp: String?,
    val country: String? = null,
    val city: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val region: String? = null,
    val zip: String? = null,
    val timezone: String? = null,
    val isp: String? = null
) {
    fun toDomain() = VelaNetworkInfo(
        localIp = localIp,
        publicIp = publicIp,
        location = if (city != null || country != null) {
            VelaLocation(
                status = "success",
                country = country,
                region = region,
                city = city,
                zip = zip,
                timezone = timezone,
                isp = isp,
                lat = lat,
                lon = lon
            )
        } else null
    )

    companion object {
        fun fromDomain(domain: VelaNetworkInfo) = VelaNetworkEntity(
            id = 0,
            localIp = domain.localIp,
            publicIp = domain.publicIp,
            country = domain.location?.country,
            city = domain.location?.city,
            lat = domain.location?.lat,
            lon = domain.location?.lon,
            region = domain.location?.region,
            zip = domain.location?.zip,
            timezone = domain.location?.timezone,
            isp = domain.location?.isp
        )
    }
}

@Entity(tableName = "vela_audio")
data class VelaAudioEntity(
    @PrimaryKey val id: Int = 0,
    val volume: Int,
    val muted: Boolean,
    val micMuted: Boolean = false,
    val activeDeviceId: String? = null
) {
    fun toDomain() = VelaAudioState(
        volume = volume, 
        muted = muted, 
        micMuted = micMuted,
        activeDeviceId = activeDeviceId
    )
    companion object {
        fun fromDomain(domain: VelaAudioState) = VelaAudioEntity(
            id = 0, 
            volume = domain.volume, 
            muted = domain.muted,
            micMuted = domain.micMuted,
            activeDeviceId = domain.activeDeviceId
        )
    }
}

@Entity(tableName = "vela_audio_devices")
data class VelaAudioDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean
) {
    fun toDomain() = VelaAudioDevice(id, name, type, isActive)
    companion object {
        fun fromDomain(domain: VelaAudioDevice) = VelaAudioDeviceEntity(
            id = domain.id,
            name = domain.name,
            type = domain.type,
            isActive = domain.isActive
        )
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
    val lengthSeconds: Double?,
    val artUrl: String? = null
) {
    fun toDomain() = VelaMediaState(title, artist, album, status, positionSeconds, lengthSeconds, artUrl)
    companion object {
        fun fromDomain(domain: VelaMediaState) = VelaMediaEntity(
            id = 0,
            title = domain.title,
            artist = domain.artist,
            album = domain.album,
            status = domain.status,
            positionSeconds = domain.positionSeconds,
            lengthSeconds = domain.lengthSeconds,
            artUrl = domain.artUrl
        )
    }
}

@Entity(tableName = "vela_processes")
data class VelaProcessEntity(
    @PrimaryKey val id: String, // Combination of pid and isTopByMemory to allow caching both lists
    val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double,
    val username: String? = null,
    val memoryRss: Long? = null,
    val isTopByMemory: Boolean = false
) {
    fun toDomain() = VelaProcess(pid, name, cpu, mem, username, memoryRss)
    companion object {
        fun fromDomain(domain: VelaProcess, isTopByMemory: Boolean = false) = VelaProcessEntity(
            id = "${domain.pid}_${if (isTopByMemory) "mem" else "cpu"}",
            pid = domain.pid,
            name = domain.name,
            cpu = domain.cpu,
            mem = domain.mem,
            username = domain.username,
            memoryRss = domain.memoryRss,
            isTopByMemory = isTopByMemory
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
    fun toDomain() = VelaDiskUsage(mountpoint, total.toString(), used.toString(), free.toString(), percent)
    companion object {
        fun fromDomain(domain: VelaDiskUsage) = VelaDiskEntity(
            mountpoint = domain.mountpoint,
            total = domain.total.toLongOrNull() ?: 0L,
            used = domain.used.toLongOrNull() ?: 0L,
            free = domain.free.toLongOrNull() ?: 0L,
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
    val device: String?,
    val signal: Int?,
    val isEnabled: Boolean
) {
    fun toDomain(networks: List<VelaWifiNetwork> = emptyList()) = VelaWifiStatus(
        connected = connected,
        ssid = ssid,
        device = device,
        signal = signal,
        isEnabled = isEnabled,
        availableNetworks = networks
    )

    companion object {
        fun fromDomain(domain: VelaWifiStatus) = VelaWifiEntity(
            id = 0,
            connected = domain.connected,
            ssid = domain.ssid,
            device = domain.device,
            signal = domain.signal,
            isEnabled = domain.isEnabled
        )
    }
}

@Entity(tableName = "vela_wifi_networks")
data class VelaWifiNetworkEntity(
    @PrimaryKey val ssid: String,
    val security: String?,
    val signal: Int?,
    val isActive: Boolean
) {
    fun toDomain() = VelaWifiNetwork(ssid, security, signal, isActive)
    companion object {
        fun fromDomain(domain: VelaWifiNetwork) = VelaWifiNetworkEntity(
            ssid = domain.ssid,
            security = domain.security,
            signal = domain.signal,
            isActive = domain.isActive
        )
    }
}

@Entity(tableName = "vela_bluetooth")
data class VelaBluetoothEntity(
    @PrimaryKey val id: Int = 0,
    val isEnabled: Boolean = true
) {
    fun toDomain(connected: List<VelaBluetoothDevice>, paired: List<VelaBluetoothDevice>) =
        VelaBluetoothStatus(connected, paired, isEnabled)
}

@Entity(tableName = "vela_bluetooth_devices")
data class VelaBluetoothDeviceEntity(
    @PrimaryKey val address: String,
    val name: String,
    val isConnected: Boolean,
    val isPaired: Boolean
) {
    fun toDomain() = VelaBluetoothDevice(
        address = address,
        name = name,
        isConnected = isConnected,
        isPaired = isPaired
    )

    companion object {
        fun fromDomain(domain: VelaBluetoothDevice) = VelaBluetoothDeviceEntity(
            address = domain.address,
            name = domain.name,
            isConnected = domain.isConnected,
            isPaired = domain.isPaired
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
    val output: String?,
    val rotation: String,
    val nightLightEnabled: Boolean,
    val nightLightTemp: Int
) {
    fun toDomain() = VelaResolution(width, height, refresh, output, rotation, nightLightEnabled, nightLightTemp)
    companion object {
        fun fromDomain(domain: VelaResolution) = VelaResolutionEntity(
            id = 0,
            width = domain.width,
            height = domain.height,
            refresh = domain.refresh,
            output = domain.output,
            rotation = domain.rotation,
            nightLightEnabled = domain.nightLightEnabled,
            nightLightTemp = domain.nightLightTemp
        )
    }
}

@Entity(tableName = "vela_cpu_usage")
data class VelaCpuUsageEntity(
    @PrimaryKey val id: Int = 0,
    val overall: Double,
    val perCore: List<Double>
) {
    fun toDomain() = VelaCpuUsage(overall, perCore)
    companion object {
        fun fromDomain(domain: VelaCpuUsage) = VelaCpuUsageEntity(id = 0, overall = domain.overall, perCore = domain.perCore)
    }
}

@Entity(tableName = "vela_ram_usage")
data class VelaRamUsageEntity(
    @PrimaryKey val id: Int = 0,
    val total: Long,
    val available: Long,
    val used: Long,
    val percent: Double,
    val swapTotal: Long,
    val swapUsed: Long,
    val swapFree: Long,
    val swapPercent: Double
) {
    fun toDomain() = VelaRamUsage(total, available, used, percent, swapTotal, swapUsed, swapFree, swapPercent)
    companion object {
        fun fromDomain(domain: VelaRamUsage) = VelaRamUsageEntity(
            id = 0,
            total = domain.total,
            available = domain.available,
            used = domain.used,
            percent = domain.percent,
            swapTotal = domain.swapTotal,
            swapUsed = domain.swapUsed,
            swapFree = domain.swapFree,
            swapPercent = domain.swapPercent
        )
    }
}

@Entity(tableName = "vela_gpu_usage")
data class VelaGpuUsageEntity(
    @PrimaryKey val name: String,
    val usagePercent: Double,
    val vramTotal: Long,
    val vramUsed: Long,
    val vramPercent: Double
) {
    fun toDomain() = VelaGpuUsage(name, usagePercent, vramTotal, vramUsed, vramPercent)
    companion object {
        fun fromDomain(domain: VelaGpuUsage) = VelaGpuUsageEntity(
            name = domain.name ?: "Unknown GPU",
            usagePercent = domain.usagePercent,
            vramTotal = domain.vramTotal,
            vramUsed = domain.vramUsed,
            vramPercent = domain.vramPercent
        )
    }
}

@Entity(tableName = "vela_disk_io")
data class VelaDiskIoEntity(
    @PrimaryKey val device: String,
    val readBytesPerSec: Double,
    val writeBytesPerSec: Double
) {
    fun toDomain() = VelaDiskIo(device, readBytesPerSec, writeBytesPerSec)
    companion object {
        fun fromDomain(domain: VelaDiskIo) = VelaDiskIoEntity(domain.device, domain.readBytesPerSec, domain.writeBytesPerSec)
    }
}

@Entity(tableName = "vela_network_io")
data class VelaNetworkIoEntity(
    @PrimaryKey val interfaceName: String,
    val bytesSentPerSec: Double,
    val bytesRecvPerSec: Double
) {
    fun toDomain() = VelaNetworkIo(interfaceName, bytesSentPerSec, bytesRecvPerSec)
    companion object {
        fun fromDomain(domain: VelaNetworkIo) = VelaNetworkIoEntity(domain.interfaceName, domain.bytesSentPerSec, domain.bytesRecvPerSec)
    }
}

@Entity(tableName = "vela_temperatures")
data class VelaTemperatureEntity(
    @PrimaryKey val id: String, // sensor + label
    val sensor: String,
    val label: String,
    val current: Double,
    val high: Double?,
    val critical: Double?
) {
    fun toDomain() = VelaTemperatureInfo(sensor, label, current, high, critical)
    companion object {
        fun fromDomain(domain: VelaTemperatureInfo) = VelaTemperatureEntity(
            id = "${domain.sensor}_${domain.label}",
            sensor = domain.sensor,
            label = domain.label,
            current = domain.current,
            high = domain.high,
            critical = domain.critical
        )
    }
}

@Entity(tableName = "vela_sensors")
data class VelaSensorEntity(
    @PrimaryKey val name: String,
    val value: String,
    val unit: String?
) {
    fun toDomain() = VelaSensorInfo(name, value, unit)
    companion object {
        fun fromDomain(domain: VelaSensorInfo) = VelaSensorEntity(
            name = domain.name,
            value = domain.value,
            unit = domain.unit
        )
    }
}

@Entity(tableName = "vela_fans")
data class VelaFanEntity(
    @PrimaryKey val id: String, // sensor + index
    val sensor: String,
    val speedRpm: Int,
    val index: Int
) {
    fun toDomain() = VelaFanInfo(sensor, speedRpm, index)
    companion object {
        fun fromDomain(domain: VelaFanInfo) = VelaFanEntity(
            id = "${domain.sensor}_${domain.index}",
            sensor = domain.sensor,
            speedRpm = domain.speedRpm,
            index = domain.index
        )
    }
}

@Entity(tableName = "vela_battery")
data class VelaBatteryEntity(
    @PrimaryKey val id: Int = 0,
    val percent: Double,
    val pluggedIn: Boolean,
    val secsLeft: Long?
) {
    fun toDomain() = VelaBatteryStatus(percent, pluggedIn, secsLeft)
    companion object {
        fun fromDomain(domain: VelaBatteryStatus) = VelaBatteryEntity(id = 0, percent = domain.percent, pluggedIn = domain.pluggedIn, secsLeft = domain.secsLeft)
    }
}

@Entity(tableName = "vela_clipboard")
data class VelaClipboardEntity(
    @PrimaryKey val id: Int = 0,
    val content: String
) {
    companion object {
        fun fromContent(content: String) = VelaClipboardEntity(id = 0, content = content)
    }
}

@Entity(tableName = "vela_active_window")
data class VelaActiveWindowEntity(
    @PrimaryKey val id: Int = 0,
    val title: String
) {
    companion object {
        fun fromTitle(title: String) = VelaActiveWindowEntity(id = 0, title = title)
    }
}

@Entity(tableName = "vela_scheduled_tasks")
data class VelaScheduledTaskEntity(
    @PrimaryKey val id: String,
    val command: String,
    val nextRun: String,
    val recurring: String?
) {
    fun toDomain() = VelaScheduledTask(id, command, nextRun, recurring)
    companion object {
        fun fromDomain(domain: VelaScheduledTask) = VelaScheduledTaskEntity(
            id = domain.id,
            command = domain.command,
            nextRun = domain.nextRun,
            recurring = domain.recurring
        )
    }
}


@Entity(tableName = "vela_files")
data class VelaFileEntity(
    @PrimaryKey val path: String,
    val parentPath: String,
    val name: String,
    val type: String,
    val size: Long,
    val modified: Double,
    val isHidden: Boolean,
    val hasChildren: Boolean,
    val childrenCount: Int?,
    val extension: String?
) {
    fun toDomain() = VelaFileInfo(
        name = name, 
        path = path, 
        type = type, 
        size = size, 
        modified = modified,
        isHidden = isHidden,
        hasChildren = hasChildren,
        childrenCount = childrenCount,
        extension = extension
    )
    companion object {
        fun fromDomain(domain: VelaFileInfo, parentPath: String) = VelaFileEntity(
            path = domain.path,
            parentPath = parentPath,
            name = domain.name,
            type = domain.type,
            size = domain.size,
            modified = domain.modified,
            isHidden = domain.isHidden,
            hasChildren = domain.hasChildren,
            childrenCount = domain.childrenCount,
            extension = domain.extension
        )
    }
}

@Entity("vela_config")
data class VelaConfigEntity(
    @PrimaryKey val id: Int = 0,
    val homeDirectory: String,
    val username: String
) {
    fun toDomain() = VelaConfig(homeDirectory, username)
    companion object {
        fun fromDomain(domain: VelaConfig) = VelaConfigEntity(id = 0, homeDirectory = domain.homeDirectory, username = domain.username)
    }
}
