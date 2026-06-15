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

// ── Filesystem ──

@JsonClass(generateAdapter = true)
data class FileItem(
    val name: String? = null,
    val path: String? = null,
    val type: String? = null,
    val size: Long? = null,
    val modified: Long? = null
)

@JsonClass(generateAdapter = true)
data class FileListResponse(
    val files: List<FileItem>? = null
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
    val total: Long? = null,
    val used: Long? = null,
    val free: Long? = null,
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
    val country: String? = null,
    val city: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
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
    val signal: Int? = null
)

@JsonClass(generateAdapter = true)
data class WifiListResponse(
    val networks: List<WifiNetwork>? = null
)

@JsonClass(generateAdapter = true)
data class WifiConnectRequest(
    val ssid: String,
    val password: String
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
    val id: String? = null,
    val name: String? = null,
    val paired: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class BluetoothDevicesResponse(
    val devices: List<BluetoothDevice>? = null
)

@JsonClass(generateAdapter = true)
data class BluetoothDeviceRequest(
    @Json(name = "device_id") val deviceId: String
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
    val data: String? = null
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
    val cpu: Double? = null,
    val mem: Double? = null
)

@JsonClass(generateAdapter = true)
data class ProcessesResponse(
    val processes: List<ProcessItem>? = null
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
    @Json(name = "next_run") val nextRun: String? = null,
    val command: String? = null,
    val recurring: String? = null
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
