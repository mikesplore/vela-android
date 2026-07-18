package com.template.app.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.*

@Entity(tableName = "vela_health")
data class VelaHealthEntity(
    @PrimaryKey val connectionId: Long,
    val status: String,
    val uptimeSeconds: Long
) {
    fun toDomain() = VelaHealth(status, uptimeSeconds)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaHealth) = VelaHealthEntity(
            connectionId = connectionId,
            status = domain.status,
            uptimeSeconds = domain.uptimeSeconds
        )
    }
}

@Entity(tableName = "vela_uptime")
data class VelaUptimeEntity(
    @PrimaryKey val connectionId: Long,
    val seconds: Int,
    val minutes: Int?,
    val hours: Int?,
    val days: Int?,
    val weeks: Int?,
    val months: Int?,
    val years: Int?,
    val formatted: String
) {
    fun toDomain() = VelaUptime(seconds, minutes, hours, days, weeks, months, years, formatted)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaUptime) = VelaUptimeEntity(
            connectionId = connectionId,
            seconds = domain.seconds,
            minutes = domain.minutes,
            hours = domain.hours,
            days = domain.days,
            weeks = domain.weeks,
            months = domain.months,
            years = domain.years,
            formatted = domain.formatted
        )
    }
}

@Entity(tableName = "NetUsageEntity")
data class NetUsageEntity(
    @PrimaryKey val connectionId: Long,
    val interfaceName: String,
    val period: String,
    val receivedBytes: Long,
    val transmittedBytes: Long,
    val received: String,
    val transmitted: String
) {
    fun toDomain() = NetUsage(interfaceName, period, receivedBytes, transmittedBytes, received, transmitted)
    companion object {
        fun fromDomain(connectionId: Long, domain: NetUsage) = NetUsageEntity(
            connectionId = connectionId,
            interfaceName = domain.interfaceName,
            period = domain.period,
            receivedBytes = domain.receivedBytes,
            transmittedBytes = domain.transmittedBytes,
            transmitted = domain.transmitted,
            received = domain.received
        )
    }
}

@Entity(tableName = "vela_device")
data class VelaDeviceEntity(
    @PrimaryKey val connectionId: Long,
    val laptopModel: String?,
    val hardwareVendor: String?,
    val osDistro: String?,
    val osDistroVersion: String?,
    val kernel: String?,
    val architecture: String?,
    val hostname: String?,
    val prettyHostname: String?
) {
    fun toDomain() = VelaDevice(
        laptopModel, hardwareVendor, osDistro, osDistroVersion,
        kernel, architecture, hostname, prettyHostname
    )
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaDevice) = VelaDeviceEntity(
            connectionId = connectionId,
            laptopModel = domain.laptopModel,
            hardwareVendor = domain.hardwareVendor,
            osDistro = domain.osDistro,
            osDistroVersion = domain.osDistroVersion,
            kernel = domain.kernel,
            architecture = domain.architecture,
            hostname = domain.hostname,
            prettyHostname = domain.prettyHostname
        )
    }
}

@Entity(tableName = "vela_network")
data class VelaNetworkEntity(
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaNetworkInfo) = VelaNetworkEntity(
            connectionId = connectionId,
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
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaAudioState) = VelaAudioEntity(
            connectionId = connectionId,
            volume = domain.volume,
            muted = domain.muted,
            micMuted = domain.micMuted,
            activeDeviceId = domain.activeDeviceId
        )
    }
}

@Entity(
    tableName = "vela_audio_devices",
    primaryKeys = ["connectionId", "id"]
)
data class VelaAudioDeviceEntity(
    val connectionId: Long,
    val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean
) {
    fun toDomain() = VelaAudioDevice(id, name, type, isActive)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaAudioDevice) = VelaAudioDeviceEntity(
            connectionId = connectionId,
            id = domain.id,
            name = domain.name,
            type = domain.type,
            isActive = domain.isActive
        )
    }
}

@Entity(tableName = "vela_media")
data class VelaMediaEntity(
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaMediaState) = VelaMediaEntity(
            connectionId = connectionId,
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

@Entity(
    tableName = "vela_processes",
    primaryKeys = ["connectionId", "id"]
)
data class VelaProcessEntity(
    val connectionId: Long,
    val id: String,
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
        fun fromDomain(connectionId: Long, domain: VelaProcess, isTopByMemory: Boolean = false) =
            VelaProcessEntity(
                connectionId = connectionId,
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

@Entity(
    tableName = "vela_disks",
    primaryKeys = ["connectionId", "mountpoint"]
)
data class VelaDiskEntity(
    val connectionId: Long,
    val mountpoint: String,
    val total: Long,
    val used: Long,
    val free: Long,
    val percent: Double
) {
    fun toDomain() = VelaDiskUsage(mountpoint, total.toString(), used.toString(), free.toString(), percent)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaDiskUsage) = VelaDiskEntity(
            connectionId = connectionId,
            mountpoint = domain.mountpoint,
            total = domain.total.toLongOrNull() ?: 0L,
            used = domain.used.toLongOrNull() ?: 0L,
            free = domain.free.toLongOrNull() ?: 0L,
            percent = domain.percent
        )
    }
}

@Entity(
    tableName = "vela_notifications",
    primaryKeys = ["connectionId", "id"]
)
data class VelaNotificationEntity(
    val connectionId: Long,
    val id: String,
    val title: String,
    val message: String,
    val appName: String?,
    val timestamp: Long
) {
    fun toDomain() = VelaNotification(id, title, message, appName, timestamp)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaNotification) = VelaNotificationEntity(
            connectionId = connectionId,
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
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaWifiStatus) = VelaWifiEntity(
            connectionId = connectionId,
            connected = domain.connected,
            ssid = domain.ssid,
            device = domain.device,
            signal = domain.signal,
            isEnabled = domain.isEnabled
        )
    }
}

@Entity(
    tableName = "vela_wifi_networks",
    primaryKeys = ["connectionId", "ssid"]
)
data class VelaWifiNetworkEntity(
    val connectionId: Long,
    val ssid: String,
    val security: String?,
    val signal: Int?,
    val isActive: Boolean
) {
    fun toDomain() = VelaWifiNetwork(ssid, security, signal, isActive)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaWifiNetwork) = VelaWifiNetworkEntity(
            connectionId = connectionId,
            ssid = domain.ssid,
            security = domain.security,
            signal = domain.signal,
            isActive = domain.isActive
        )
    }
}

@Entity(tableName = "vela_bluetooth")
data class VelaBluetoothEntity(
    @PrimaryKey val connectionId: Long,
    val isEnabled: Boolean = true
) {
    fun toDomain(connected: List<VelaBluetoothDevice>, paired: List<VelaBluetoothDevice>) =
        VelaBluetoothStatus(connected, paired, isEnabled)
}

@Entity(
    tableName = "vela_bluetooth_devices",
    primaryKeys = ["connectionId", "address"]
)
data class VelaBluetoothDeviceEntity(
    val connectionId: Long,
    val address: String,
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
        fun fromDomain(connectionId: Long, domain: VelaBluetoothDevice) = VelaBluetoothDeviceEntity(
            connectionId = connectionId,
            address = domain.address,
            name = domain.name,
            isConnected = domain.isConnected,
            isPaired = domain.isPaired
        )
    }
}

@Entity(tableName = "vela_brightness")
data class VelaBrightnessEntity(
    @PrimaryKey val connectionId: Long,
    val value: Int
) {
    fun toDomain() = VelaBrightness(value)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaBrightness) =
            VelaBrightnessEntity(connectionId = connectionId, value = domain.value)
    }
}

@Entity(tableName = "vela_resolution")
data class VelaResolutionEntity(
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaResolution) = VelaResolutionEntity(
            connectionId = connectionId,
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
    @PrimaryKey val connectionId: Long,
    val overall: Double,
    val perCore: List<Double>
) {
    fun toDomain() = VelaCpuUsage(overall, perCore)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaCpuUsage) =
            VelaCpuUsageEntity(connectionId = connectionId, overall = domain.overall, perCore = domain.perCore)
    }
}

@Entity(tableName = "vela_ram_usage")
data class VelaRamUsageEntity(
    @PrimaryKey val connectionId: Long,
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
        fun fromDomain(connectionId: Long, domain: VelaRamUsage) = VelaRamUsageEntity(
            connectionId = connectionId,
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

@Entity(
    tableName = "vela_gpu_usage",
    primaryKeys = ["connectionId", "name"]
)
data class VelaGpuUsageEntity(
    val connectionId: Long,
    val name: String,
    val usagePercent: Double,
    val vramTotal: Long,
    val vramUsed: Long,
    val vramPercent: Double
) {
    fun toDomain() = VelaGpuUsage(name, usagePercent, vramTotal, vramUsed, vramPercent)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaGpuUsage) = VelaGpuUsageEntity(
            connectionId = connectionId,
            name = domain.name ?: "Unknown GPU",
            usagePercent = domain.usagePercent,
            vramTotal = domain.vramTotal,
            vramUsed = domain.vramUsed,
            vramPercent = domain.vramPercent
        )
    }
}

@Entity(
    tableName = "vela_disk_io",
    primaryKeys = ["connectionId", "device"]
)
data class VelaDiskIoEntity(
    val connectionId: Long,
    val device: String,
    val readBytesPerSec: Double,
    val writeBytesPerSec: Double
) {
    fun toDomain() = VelaDiskIo(device, readBytesPerSec, writeBytesPerSec)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaDiskIo) =
            VelaDiskIoEntity(connectionId, domain.device, domain.readBytesPerSec, domain.writeBytesPerSec)
    }
}

@Entity(
    tableName = "vela_network_io",
    primaryKeys = ["connectionId", "interfaceName"]
)
data class VelaNetworkIoEntity(
    val connectionId: Long,
    val interfaceName: String,
    val bytesSentPerSec: Double,
    val bytesRecvPerSec: Double
) {
    fun toDomain() = VelaNetworkIo(interfaceName, bytesSentPerSec, bytesRecvPerSec)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaNetworkIo) =
            VelaNetworkIoEntity(connectionId, domain.interfaceName, domain.bytesSentPerSec, domain.bytesRecvPerSec)
    }
}

@Entity(
    tableName = "vela_temperatures",
    primaryKeys = ["connectionId", "id"]
)
data class VelaTemperatureEntity(
    val connectionId: Long,
    val id: String,
    val sensor: String,
    val label: String,
    val current: Double,
    val high: Double?,
    val critical: Double?
) {
    fun toDomain() = VelaTemperatureInfo(sensor, label, current, high, critical)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaTemperatureInfo) = VelaTemperatureEntity(
            connectionId = connectionId,
            id = "${domain.sensor}_${domain.label}",
            sensor = domain.sensor,
            label = domain.label,
            current = domain.current,
            high = domain.high,
            critical = domain.critical
        )
    }
}

@Entity(
    tableName = "vela_sensors",
    primaryKeys = ["connectionId", "name"]
)
data class VelaSensorEntity(
    val connectionId: Long,
    val name: String,
    val value: String,
    val unit: String?
) {
    fun toDomain() = VelaSensorInfo(name, value, unit)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaSensorInfo) = VelaSensorEntity(
            connectionId = connectionId,
            name = domain.name,
            value = domain.value,
            unit = domain.unit
        )
    }
}

@Entity(
    tableName = "vela_fans",
    primaryKeys = ["connectionId", "id"]
)
data class VelaFanEntity(
    val connectionId: Long,
    val id: String,
    val sensor: String,
    val speedRpm: Int,
    val index: Int
) {
    fun toDomain() = VelaFanInfo(sensor, speedRpm, index)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaFanInfo) = VelaFanEntity(
            connectionId = connectionId,
            id = "${domain.sensor}_${domain.index}",
            sensor = domain.sensor,
            speedRpm = domain.speedRpm,
            index = domain.index
        )
    }
}

@Entity(tableName = "vela_battery")
data class VelaBatteryEntity(
    @PrimaryKey val connectionId: Long,
    val percent: Double,
    val pluggedIn: Boolean,
    val secsLeft: Long?
) {
    fun toDomain() = VelaBatteryStatus(percent, pluggedIn, secsLeft)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaBatteryStatus) = VelaBatteryEntity(
            connectionId = connectionId,
            percent = domain.percent,
            pluggedIn = domain.pluggedIn,
            secsLeft = domain.secsLeft
        )
    }
}

@Entity(tableName = "vela_clipboard")
data class VelaClipboardEntity(
    @PrimaryKey val connectionId: Long,
    val content: String
) {
    companion object {
        fun fromContent(connectionId: Long, content: String) =
            VelaClipboardEntity(connectionId = connectionId, content = content)
    }
}

@Entity(tableName = "vela_active_window")
data class VelaActiveWindowEntity(
    @PrimaryKey val connectionId: Long,
    val title: String
) {
    companion object {
        fun fromTitle(connectionId: Long, title: String) =
            VelaActiveWindowEntity(connectionId = connectionId, title = title)
    }
}

@Entity(
    tableName = "vela_scheduled_tasks",
    primaryKeys = ["connectionId", "id"]
)
data class VelaScheduledTaskEntity(
    val connectionId: Long,
    val id: String,
    val command: String,
    val nextRun: String,
    val recurring: String?
) {
    fun toDomain() = VelaScheduledTask(id, command, nextRun, recurring)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaScheduledTask) = VelaScheduledTaskEntity(
            connectionId = connectionId,
            id = domain.id,
            command = domain.command,
            nextRun = domain.nextRun,
            recurring = domain.recurring
        )
    }
}

@Entity(
    tableName = "vela_services",
    primaryKeys = ["connectionId", "name"]
)
data class VelaServiceEntity(
    val connectionId: Long,
    val name: String,
    val load: String,
    val active: String,
    val sub: String,
    val description: String
) {
    fun toDomain() = VelaService(
        name = name,
        load = load,
        active = active,
        sub = sub,
        description = description
    )

    companion object {
        fun fromDomain(connectionId: Long, domain: VelaService) = VelaServiceEntity(
            connectionId = connectionId,
            name = domain.name,
            load = domain.load,
            active = domain.active,
            sub = domain.sub,
            description = domain.description
        )
    }
}

@Entity(
    tableName = "vela_files",
    primaryKeys = ["connectionId", "path"]
)
data class VelaFileEntity(
    val connectionId: Long,
    val path: String,
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
        fun fromDomain(connectionId: Long, domain: VelaFileInfo, parentPath: String) = VelaFileEntity(
            connectionId = connectionId,
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

@Entity(tableName = "vela_config")
data class VelaConfigEntity(
    @PrimaryKey val connectionId: Long,
    val homeDirectory: String,
    val username: String
) {
    fun toDomain() = VelaConfig(homeDirectory, username)
    companion object {
        fun fromDomain(connectionId: Long, domain: VelaConfig) = VelaConfigEntity(
            connectionId = connectionId,
            homeDirectory = domain.homeDirectory,
            username = domain.username
        )
    }
}
