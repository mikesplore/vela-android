package com.template.app.domain.model

data class VelaHealth(
    val status: String,
    val uptimeSeconds: Long,
    val version: String = "1.2.4"
)

data class VelaDevice(
    val laptopModel: String?,
    val hardwareVendor: String?,
    val osDistro: String?,
    val osDistroVersion: String?,
    val kernel: String?,
    val architecture: String?,
    val hostname: String?,
    val prettyHostname: String?
)

data class VelaUptime(
    val seconds: Int,
    val minutes: Int?,
    val hours: Int?,
    val days: Int?,
    val weeks: Int?,
    val months: Int?,
    val years: Int?,
    val formatted: String
)

data class NetUsage(
    val interfaceName: String,
    val period: String,
    val receivedBytes: Long,
    val transmittedBytes: Long,
    val received: String,
    val transmitted: String
)


data class VelaNetworkInfo(
    val localIp: String,
    val publicIp: String?,
    val location: VelaLocation? = null
)

data class VelaLocation(
    val status: String?,
    val country: String?,
    val region: String?,
    val city: String?,
    val zip: String?,
    val timezone: String?,
    val isp: String?,
    val lat: Double?,
    val lon: Double?
)

data class VelaAudioState(
    val volume: Int,
    val muted: Boolean,
    val micMuted: Boolean = false,
    val activeDeviceId: String? = null
)

data class VelaAudioDevice(
    val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean = false
)

data class VelaMediaState(
    val title: String?,
    val artist: String?,
    val album: String?,
    val status: String?,
    val positionSeconds: Double?,
    val lengthSeconds: Double?,
    val artUrl: String? = null
)

data class VelaProcess(
    val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double,
    val username: String? = null,
    val memoryRss: Long? = null
)

data class VelaDiskUsage(
    val mountpoint: String,
    val total: String,
    val used: String,
    val free: String,
    val percent: Double
)

data class VelaNotification(
    val id: String,
    val title: String,
    val message: String,
    val appName: String?,
    val timestamp: Long
)

data class VelaWifiStatus(
    val connected: Boolean,
    val ssid: String?,
    val device: String?,
    val signal: Int?,
    val isEnabled: Boolean = true,
    val availableNetworks: List<VelaWifiNetwork> = emptyList()
)

data class VelaWifiNetwork(
    val ssid: String,
    val security: String?,
    val signal: Int?,
    val isActive: Boolean
)

data class VelaPingResult(
    val host: String,
    val lossPercent: Double,
    val avgRttMs: Double,
    val transmitted: Int,
    val received: Int
)

data class VelaSpeedTest(
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double
)

data class VelaBluetoothStatus(
    val connectedDevices: List<VelaBluetoothDevice>,
    val pairedDevices: List<VelaBluetoothDevice>,
    val isEnabled: Boolean = true
)

data class VelaBluetoothDevice(
    val address: String,
    val name: String,
    val isConnected: Boolean,
    val isPaired: Boolean
)

data class VelaBrightness(
    val value: Int
)

data class VelaResolution(
    val width: Int,
    val height: Int,
    val refresh: Double,
    val output: String?,
    val rotation: String = "normal",
    val nightLightEnabled: Boolean = false,
    val nightLightTemp: Int = 4500
)

data class VelaCpuUsage(
    val overall: Double,
    val perCore: List<Double> = emptyList()
)

data class VelaRamUsage(
    val total: Long = 0,
    val available: Long = 0,
    val used: Long = 0,
    val percent: Double,
    val swapTotal: Long = 0,
    val swapUsed: Long = 0,
    val swapFree: Long = 0,
    val swapPercent: Double = 0.0
)

data class VelaGpuUsage(
    val name: String? = null,
    val usagePercent: Double = 0.0,
    val vramTotal: Long = 0,
    val vramUsed: Long = 0,
    val vramPercent: Double = 0.0
)

data class VelaDiskIo(
    val device: String,
    val readBytesPerSec: Double,
    val writeBytesPerSec: Double
)

data class VelaNetworkIo(
    val interfaceName: String,
    val bytesSentPerSec: Double,
    val bytesRecvPerSec: Double
)

data class VelaTemperatureInfo(
    val sensor: String,
    val label: String,
    val current: Double,
    val high: Double? = null,
    val critical: Double? = null
)

data class VelaFanInfo(
    val sensor: String,
    val speedRpm: Int,
    val index: Int = 0
)

data class VelaSensorInfo(
    val name: String,
    val value: String,
    val unit: String? = null
)

data class VelaBatteryStatus(
    val percent: Double,
    val pluggedIn: Boolean,
    val secsLeft: Long? = null
)

data class VelaMonitorSnapshot(
    val cpu: VelaCpuUsage,
    val ram: VelaRamUsage,
    val gpu: List<VelaGpuUsage>,
    val diskIo: List<VelaDiskIo>,
    val networkIo: List<VelaNetworkIo>,
    val temperatures: List<VelaTemperatureInfo>,
    val fans: List<VelaFanInfo>,
    val sensors: List<VelaSensorInfo> = emptyList(),
    val battery: VelaBatteryStatus?,
    val topProcessesByCpu: List<VelaProcess>,
    val topProcessesByMemory: List<VelaProcess>
)

data class VelaFileInfo(
    val name: String,
    val path: String,
    val type: String,
    val size: Long,
    val modified: Double,
    val isHidden: Boolean = false,
    val hasChildren: Boolean = false,
    val childrenCount: Int? = null,
    val extension: String? = null
)

data class VelaFileList(
    val currentPath: String,
    val parentPath: String?,
    val totalItems: Int,
    val showHidden: Boolean,
    val files: List<VelaFileInfo>
)

data class VelaBreadcrumb(
    val name: String,
    val path: String
)

data class VelaFileTree(
    val root: VelaFileInfo,
    val children: List<VelaFileInfo>,
    val breadcrumbs: List<VelaBreadcrumb>
)

data class VelaClipboard(
    val content: String
)

data class VelaScheduledTask(
    val id: String,
    val command: String,
    val nextRun: String,
    val recurring: String? = null,
    val trigger: String? = null
)

// --- Maintenance Models ---

data class VelaMaintenanceUpdate(
    val manager: String,
    val packages: List<VelaPackageUpdate>
)

data class VelaPackageUpdate(
    val packageName: String,
    val current: String? = null,
    val available: String? = null
)

data class VelaService(
    val name: String,
    val load: String = "",
    val active: String = "inactive",
    val sub: String = "",
    val description: String = ""
) {
    val isRunning: Boolean
        get() = active.equals("active", ignoreCase = true) ||
            sub.equals("running", ignoreCase = true)
}

data class VelaLogs(
    val service: String,
    val lines: List<String>
)

data class VelaConfig(
    val homeDirectory: String,
    val username: String
)
