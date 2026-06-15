package com.template.app.domain.model

data class VelaHealth(
    val status: String,
    val uptimeSeconds: Long
)

data class VelaFileInfo(
    val name: String,
    val path: String,
    val type: String,
    val size: Long,
    val modified: Long
)

data class VelaDiskUsage(
    val mountpoint: String,
    val total: Long,
    val used: Long,
    val free: Long,
    val percent: Double
)

data class VelaNetworkInfo(
    val localIp: String,
    val publicIp: String,
    val interfaceName: String
)

data class VelaProcess(
    val pid: Int,
    val name: String,
    val cpu: Double,
    val mem: Double
)

data class VelaAudioState(
    val volume: Int,
    val muted: Boolean
)

data class VelaMediaState(
    val title: String?,
    val artist: String?,
    val album: String?,
    val status: String?,
    val positionSeconds: Double?,
    val lengthSeconds: Double?
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
    val signal: Int?
)

data class VelaBrightness(
    val value: Int
)

data class VelaResolution(
    val width: Int,
    val height: Int,
    val refresh: Double,
    val output: String?
)
