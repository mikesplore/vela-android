package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.*

interface VelaRepository {
    // General
    suspend fun getHealth(): Resource<VelaHealth>
    
    // Display
    suspend fun getScreenshot(): Resource<String> // base64
    suspend fun setBrightness(value: Int): Resource<Unit>
    suspend fun lockDisplay(): Resource<Unit>
    suspend fun getResolution(): Resource<String>
    
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

    suspend fun getBrightness(): Resource<Int>
}
