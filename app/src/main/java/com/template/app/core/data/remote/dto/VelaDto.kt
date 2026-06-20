package com.template.app.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── General ──

@JsonClass(generateAdapter = true)
data class RootResponse(
    val name: String? = null,
    val version: String? = null,
    @Json(name = "enabled_modules") val enabledModules: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String? = null,
    @Json(name = "uptime_seconds") val uptimeSeconds: Long? = null
)

@JsonClass(generateAdapter = true)
data class PingResponse(
    val pong: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val success: Boolean,
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null
)

// ── Display ──

@JsonClass(generateAdapter = true)
data class ScreenshotResponse(
    @Json(name = "image_base64") val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class RecordRequest(
    @Json(name = "duration_seconds") val durationSeconds: Int
)

@JsonClass(generateAdapter = true)
data class RecordResponse(
    @Json(name = "image_base64") val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class BrightnessResponse(
    val brightness: Double? = null
)

@JsonClass(generateAdapter = true)
data class BrightnessRequest(
    val value: Int
)

@JsonClass(generateAdapter = true)
data class ResolutionResponse(
    val width: Int? = null,
    val height: Int? = null,
    val refresh: Double? = null,
    val output: String? = null
)

@JsonClass(generateAdapter = true)
data class ResolutionRequest(
    val width: Int,
    val height: Int,
    val refresh: Int
)

@JsonClass(generateAdapter = true)
data class RotateRequest(
    val orientation: String
)

@JsonClass(generateAdapter = true)
data class NightLightRequest(
    val enabled: Boolean,
    val temperature: Int? = null
)

// ── Audio ──

@JsonClass(generateAdapter = true)
data class AudioVolumeResponse(
    val volume: Int? = null,
    val muted: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class AudioVolumeRequest(
    val value: Int
)

@JsonClass(generateAdapter = true)
data class AudioStepRequest(
    val step: Int = 5
)

@JsonClass(generateAdapter = true)
data class AudioMuteRequest(
    val muted: Boolean
)

@JsonClass(generateAdapter = true)
data class AudioDevice(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class AudioOutputDeviceRequest(
    @Json(name = "device_id") val deviceId: String
)

// ── Power ──

@JsonClass(generateAdapter = true)
data class ScheduleShutdownRequest(
    val at: String
)

@JsonClass(generateAdapter = true)
data class PowerProfileResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val profile: String? = null
)

@JsonClass(generateAdapter = true)
data class PowerProfileRequest(
    val profile: String
)

// ── Filesystem ──

@JsonClass(generateAdapter = true)
data class FileItem(
    val name: String? = null,
    val path: String? = null,
    val type: String? = null,
    val size: Long? = null,
    val modified: Double? = null,
    @Json(name = "is_hidden") val isHidden: Boolean? = null,
    @Json(name = "has_children") val hasChildren: Boolean? = null,
    @Json(name = "children_count") val childrenCount: Int? = null,
    val extension: String? = null
)

@JsonClass(generateAdapter = true)
data class FileListResponse(
    @Json(name = "current_path") val currentPath: String? = null,
    @Json(name = "parent_path") val parentPath: String? = null,
    @Json(name = "total_items") val totalItems: Int? = null,
    @Json(name = "show_hidden") val showHidden: Boolean? = null,
    val files: List<FileItem>? = null
)

@JsonClass(generateAdapter = true)
data class Breadcrumb(
    val name: String? = null,
    val path: String? = null
)

@JsonClass(generateAdapter = true)
data class FileTreeResponse(
    val root: FileItem? = null,
    val children: List<FileItem>? = null,
    val breadcrumbs: List<Breadcrumb>? = null
)

@JsonClass(generateAdapter = true)
data class FilePathRequest(
    val path: String
)

@JsonClass(generateAdapter = true)
data class FileRenameRequest(
    val from: String,
    val to: String
)

@JsonClass(generateAdapter = true)
data class DiskUsageItem(
    val mountpoint: String? = null,
    val total: String? = null,
    val used: String? = null,
    val free: String? = null,
    val percent: Double? = null,
    val filesystem: String? = null
)

@JsonClass(generateAdapter = true)
data class DiskUsageResponse(
    val usage: List<DiskUsageItem>? = null
)

@JsonClass(generateAdapter = true)
data class ZipRequest(
    val paths: List<String>,
    val output: String
)

@JsonClass(generateAdapter = true)
data class UnzipRequest(
    val path: String,
    val destination: String
)

// ── Network ──

@JsonClass(generateAdapter = true)
data class NetworkIpResponse(
    @Json(name = "local_ip") val localIp: String? = null,
    @Json(name = "public_ip") val publicIp: String? = null,
    @Json(name = "interface_name") val interfaceName: String? = null
)

@JsonClass(generateAdapter = true)
data class GeoLocation(
    val status: String? = null,
    val query: String? = null,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val timezone: String? = null,
    val isp: String? = null,
    val org: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkLocationResponse(
    @Json(name = "local_ip") val localIp: String? = null,
    @Json(name = "public_ip") val publicIp: String? = null,
    val location: GeoLocation? = null
)

@JsonClass(generateAdapter = true)
data class WifiStatusResponse(
    val connected: Boolean? = null,
    val ssid: String? = null,
    val device: String? = null,
    val signal: Int? = null,
    val networks: List<WifiNetwork>? = null
)

@JsonClass(generateAdapter = true)
data class WifiNetwork(
    val ssid: String? = null,
    val security: String? = null,
    val signal: Int? = null,
    val active: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class WifiConnectRequest(
    val ssid: String,
    val password: String? = null
)

@JsonClass(generateAdapter = true)
data class WifiToggleRequest(
    val enabled: Boolean
)

@JsonClass(generateAdapter = true)
data class PingHostRequest(
    val host: String,
    val count: Int = 4
)

@JsonClass(generateAdapter = true)
data class PingHostResponse(
    val host: String? = null,
    @Json(name = "packets_transmitted") val packetsTransmitted: Int? = null,
    @Json(name = "packets_received") val packetsReceived: Int? = null,
    @Json(name = "packet_loss") val packetLoss: Double? = null,
    @Json(name = "avg_rtt_ms") val avgRttMs: Double? = null
)

@JsonClass(generateAdapter = true)
data class SpeedTestResponse(
    @Json(name = "download_mbps") val downloadMbps: Double? = null,
    @Json(name = "upload_mbps") val uploadMbps: Double? = null,
    @Json(name = "ping_ms") val pingMs: Double? = null
)

@JsonClass(generateAdapter = true)
data class BluetoothDevice(
    val address: String? = null,
    val name: String? = null,
    val connected: Boolean? = null,
    val paired: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class BluetoothDevicesResponse(
    @Json(name = "connected_devices") val connectedDevices: List<BluetoothDevice>? = null,
    @Json(name = "paired_devices") val pairedDevices: List<BluetoothDevice>? = null
)

@JsonClass(generateAdapter = true)
data class BluetoothToggleRequest(
    val enabled: Boolean
)

@JsonClass(generateAdapter = true)
data class BluetoothDeviceRequest(
    val address: String
)

@JsonClass(generateAdapter = true)
data class BluetoothActionResponse(
    val address: String? = null,
    val action: String? = null,
    val message: String? = null
)

// ── Notifications ──

@JsonClass(generateAdapter = true)
data class SendNotificationRequest(
    val title: String,
    val message: String,
    val urgency: String = "normal",
    @Json(name = "app_name") val appName: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: Any? = null,
    val title: String? = null,
    val message: String? = null,
    @Json(name = "app_name") val appName: String? = null,
    val urgency: String? = null,
    val timestamp: Any? = null
)

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    val notifications: List<NotificationItem>? = null
)

// ── Clipboard ──

@JsonClass(generateAdapter = true)
data class ClipboardResponse(
    @Json(name = "text") val data: String? = null
)

@JsonClass(generateAdapter = true)
data class ClipboardWriteRequest(
    val text: String
)

// ── Media ──

@JsonClass(generateAdapter = true)
data class MediaNowPlayingResponse(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val status: String? = null,

    @Json(name = "art_url") val artUrl: String? = null,
    @Json(name = "position_seconds") val positionSeconds: Double? = null,
    @Json(name = "length_seconds") val lengthSeconds: Double? = null
)

@JsonClass(generateAdapter = true)
data class MediaSeekRequest(
    val seconds: Int
)

// ── Processes ──

@JsonClass(generateAdapter = true)
data class ProcessItem(
    val pid: Int? = null,
    val name: String? = null,
    val username: String? = null,
    @Json(name = "cpu_percent") val cpu: Double? = null,
    @Json(name = "memory_percent") val mem: Double? = null,
    @Json(name = "memory_rss") val memRss: Long? = null
)

@JsonClass(generateAdapter = true)
data class ProcessesResponse(
    @Json(name = "top_by_cpu") val topByCpu: List<ProcessItem>? = null,
    @Json(name = "top_by_memory") val topByMemory: List<ProcessItem>? = null
)

@JsonClass(generateAdapter = true)
data class ProcessLaunchRequest(
    val command: String,
    val args: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ActiveWindowResponse(
    val title: String? = null
)

@JsonClass(generateAdapter = true)
data class WindowActionRequest(
    @Json(name = "window_id") val windowId: String
)

// ── Monitor ──

@JsonClass(generateAdapter = true)
data class CpuResponse(
    val overall: Double? = null,
    @Json(name = "per_core") val perCore: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class RamResponse(
    val total: Long? = null,
    val available: Long? = null,
    val used: Long? = null,
    val percent: Double? = null,
    @Json(name = "swap_total") val swapTotal: Long? = null,
    @Json(name = "swap_used") val swapUsed: Long? = null,
    @Json(name = "swap_free") val swapFree: Long? = null,
    @Json(name = "swap_percent") val swapPercent: Double? = null
)

@JsonClass(generateAdapter = true)
data class GpuItem(
    val name: String? = null,
    @Json(name = "usage_percent") val usagePercent: Double? = null,
    @Json(name = "vram_total") val vramTotal: Long? = null,
    @Json(name = "vram_used") val vramUsed: Long? = null,
    @Json(name = "vram_percent") val vramPercent: Double? = null
)

@JsonClass(generateAdapter = true)
data class DiskIoItem(
    val device: String? = null,
    @Json(name = "read_bytes_per_sec") val readBytesPerSec: Double? = null,
    @Json(name = "write_bytes_per_sec") val writeBytesPerSec: Double? = null
)

@JsonClass(generateAdapter = true)
data class NetworkIoItem(
    @Json(name = "interface") val interfaceName: String? = null,
    @Json(name = "bytes_sent_per_sec") val bytesSentPerSec: Double? = null,
    @Json(name = "bytes_recv_per_sec") val bytesRecvPerSec: Double? = null
)

@JsonClass(generateAdapter = true)
data class SensorItem(
    val sensor: String? = null,
    val label: String? = null,
    val current: Double? = null,
    val high: Double? = null,
    val critical: Double? = null
)

@JsonClass(generateAdapter = true)
data class FanItem(
    val sensor: String? = null,
    @Json(name = "speed_rpm") val speedRpm: Int? = null
)

@JsonClass(generateAdapter = true)
data class BatteryResponse(
    val percent: Double? = null,
    @Json(name = "plugged_in") val pluggedIn: Boolean? = null,
    @Json(name = "secs_left") val secsLeft: Long? = null
)

@JsonClass(generateAdapter = true)
data class MonitorSnapshotResponse(
    val cpu: CpuResponse? = null,
    val ram: RamResponse? = null,
    val gpu: List<GpuItem>? = null,
    @Json(name = "disk_io") val diskIo: List<DiskIoItem>? = null,
    @Json(name = "network_io") val networkIo: List<NetworkIoItem>? = null,
    val temperatures: List<SensorItem>? = null,
    val fans: List<FanItem>? = null,
    val battery: BatteryResponse? = null,
    val processes: ProcessesResponse? = null
)

// ── Input ──

@JsonClass(generateAdapter = true)
data class MouseMoveRequest(
    val x: Int,
    val y: Int
)

@JsonClass(generateAdapter = true)
data class MousePositionRequest(
    val x: Int,
    val y: Int
)

@JsonClass(generateAdapter = true)
data class MouseClickRequest(
    val x: Int,
    val y: Int,
    val button: String = "left"
)

@JsonClass(generateAdapter = true)
data class MouseScrollRequest(
    val direction: String,
    val amount: Int = 3
)

@JsonClass(generateAdapter = true)
data class KeyboardTypeRequest(
    val text: String
)

@JsonClass(generateAdapter = true)
data class KeyboardKeyRequest(
    val keys: List<String>
)

// ── Security ──

@JsonClass(generateAdapter = true)
data class WebcamSnapshotResponse(
    @Json(name = "image_base64") val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginEvent(
    val `when`: Long? = null,
    val user: String? = null,
    val type: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginHistoryResponse(
    val events: List<LoginEvent>? = null
)

@JsonClass(generateAdapter = true)
data class SshSession(
    val pid: Int? = null,
    val user: String? = null,
    val remote: String? = null
)

@JsonClass(generateAdapter = true)
data class SshSessionsResponse(
    val sessions: List<SshSession>? = null
)

// ── Scheduler ──

@JsonClass(generateAdapter = true)
data class SchedulerCreateRequest(
    val command: String,
    @Json(name = "run_at") val runAt: String,
    val recurring: String? = null
)

@JsonClass(generateAdapter = true)
data class ScheduledTask(
    val id: String? = null,
    @Json(name = "next_run_time") val nextRun: String? = null,
    @Json(name = "run_at") val runAt: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val recurring: String? = null
)

@JsonClass(generateAdapter = true)
data class SchedulerListResponse(
    val jobs: List<ScheduledTask>? = null
)

// ── Maintenance ──

@JsonClass(generateAdapter = true)
data class LogsResponse(
    val service: String? = null,
    val lines: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class PackageUpdate(
    val name: String? = null,
    val version: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdatesResponse(
    @Json(name = "updates_available") val updatesAvailable: Boolean? = null,
    val packages: List<PackageUpdate>? = null
)

@JsonClass(generateAdapter = true)
data class ServiceItem(
    val name: String? = null,
    val active: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class ServicesResponse(
    val services: List<ServiceItem>? = null
)

@JsonClass(generateAdapter = true)
data class ServiceActionRequest(
    val name: String
)

@JsonClass(generateAdapter = true)
data class VelaConfigDto(
    @Json(name = "home_directory") val homeDirectory: String,
    val username: String,
)
