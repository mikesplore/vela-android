package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface VelaRepository {
    // Observable streams (Offline-first)
    fun observeHealth(): Flow<VelaHealth?>
    fun observeNetwork(): Flow<VelaNetworkInfo?>
    fun observeAudio(): Flow<VelaAudioState?>
    fun observeMedia(): Flow<VelaMediaState?>
    fun observeProcesses(limit: Int = 5): Flow<List<VelaProcess>>
    fun observeDisks(): Flow<List<VelaDiskUsage>>
    fun observeNotifications(): Flow<List<VelaNotification>>
    fun observeWifi(): Flow<VelaWifiStatus?>
    fun observeBrightness(): Flow<VelaBrightness?>
    fun observeResolution(): Flow<VelaResolution?>
    fun observeCpuUsage(): Flow<VelaCpuUsage?>
    fun observeRamUsage(): Flow<VelaRamUsage?>
    fun observeClipboard(): Flow<VelaClipboard?>
    fun observeActiveWindow(): Flow<String?>

    // General
    suspend fun getHealth(): Resource<VelaHealth>
    
    // Display
    suspend fun getScreenshot(): Resource<String> // base64
    suspend fun setBrightness(value: Int): Resource<Unit>
    suspend fun lockDisplay(): Resource<Unit>
    suspend fun getResolution(): Resource<String>
    suspend fun getBrightness(): Resource<Int>
    
    // Audio
    suspend fun getVolume(): Resource<VelaAudioState>
    suspend fun setVolume(value: Int): Resource<VelaAudioState>
    suspend fun setMute(muted: Boolean): Resource<VelaAudioState>
    
    // Power
    suspend fun shutdown(): Resource<Unit>
    suspend fun restart(): Resource<Unit>
    suspend fun sleep(): Resource<Unit>
    suspend fun hibernate(): Resource<Unit>
    suspend fun scheduleShutdown(at: String): Resource<Unit>
    suspend fun cancelShutdown(): Resource<Unit>
    suspend fun getPowerProfile(): Resource<String>
    suspend fun setPowerProfile(profile: String): Resource<Unit>
    
    // Filesystem
    suspend fun listFiles(path: String): Resource<List<VelaFileInfo>>
    suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>>
    
    // Network
    suspend fun getNetworkInfo(): Resource<VelaNetworkInfo>
    suspend fun getNetworkLocation(): Resource<VelaNetworkInfo>
    suspend fun getWifiStatus(): Resource<VelaWifiStatus>
    suspend fun getWifiList(): Resource<List<VelaWifiNetwork>>
    suspend fun connectWifi(ssid: String, password: String): Resource<Unit>
    suspend fun disconnectWifi(): Resource<Unit>
    suspend fun toggleWifi(enabled: Boolean): Resource<Unit>
    suspend fun pingHost(host: String, count: Int): Resource<VelaPingResult>
    suspend fun runSpeedTest(): Resource<VelaSpeedTest>
    suspend fun getBluetoothDevices(): Resource<List<VelaBluetoothDevice>>
    suspend fun pairBluetooth(deviceId: String): Resource<Unit>
    suspend fun unpairBluetooth(deviceId: String): Resource<Unit>
    
    // Notifications
    suspend fun getNotifications(): Resource<List<VelaNotification>>
    
    // Clipboard
    suspend fun readClipboard(): Resource<String>
    suspend fun writeClipboard(text: String): Resource<Unit>
    suspend fun clearClipboard(): Resource<Unit>
    
    // Media
    suspend fun getNowPlaying(): Resource<VelaMediaState?>
    suspend fun togglePlayPause(): Resource<Unit>
    suspend fun mediaNext(): Resource<Unit>
    suspend fun mediaPrevious(): Resource<Unit>
    suspend fun mediaSeek(seconds: Int): Resource<Unit>
    
    // Processes
    suspend fun getProcesses(): Resource<List<VelaProcess>>
    suspend fun getActiveWindow(): Resource<String>
    suspend fun killProcess(pid: Int): Resource<Unit>

    // Monitor
    suspend fun getCpuUsage(): Resource<VelaCpuUsage>
    suspend fun getRamUsage(): Resource<VelaRamUsage>
}
