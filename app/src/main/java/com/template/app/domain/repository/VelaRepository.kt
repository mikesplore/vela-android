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
    
    // Filesystem
    suspend fun listFiles(path: String): Resource<List<VelaFileInfo>>
    suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>>
    
    // Network
    suspend fun getNetworkInfo(): Resource<VelaNetworkInfo>
    suspend fun getWifiStatus(): Resource<String> // SSID
    
    // Notifications
    suspend fun getNotifications(): Resource<List<VelaNotification>>
    
    // Clipboard
    suspend fun readClipboard(): Resource<String>
    suspend fun writeClipboard(text: String): Resource<Unit>
    
    // Media
    suspend fun getNowPlaying(): Resource<VelaMediaState?>
    suspend fun togglePlayPause(): Resource<Unit>
    
    // Processes
    suspend fun getProcesses(): Resource<List<VelaProcess>>
    suspend fun getActiveWindow(): Resource<String>
}
