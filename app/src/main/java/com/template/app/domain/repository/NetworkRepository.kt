package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun observeNetwork(): Flow<VelaNetworkInfo?>
    fun observeWifi(): Flow<VelaWifiStatus?>
    fun observeBluetooth(): Flow<VelaBluetoothStatus?>

    suspend fun getNetworkInfo(): Resource<VelaNetworkInfo>
    suspend fun getNetworkLocation(): Resource<VelaNetworkInfo>
    
    suspend fun getWifiStatus(): Resource<VelaWifiStatus>
    suspend fun getWifiList(): Resource<VelaWifiStatus>
    suspend fun connectWifi(ssid: String, password: String? = null): Resource<VelaWifiStatus>
    suspend fun disconnectWifi(): Resource<VelaNetworkInfo>
    suspend fun toggleWifi(enabled: Boolean): Resource<VelaNetworkInfo>
    
    suspend fun toggleBluetooth(enabled: Boolean): Resource<VelaNetworkInfo>
    suspend fun getBluetoothDevices(): Resource<VelaBluetoothStatus>
    suspend fun pairBluetooth(address: String): Resource<Unit>
    suspend fun connectBluetooth(address: String): Resource<Unit>
    suspend fun disconnectBluetooth(address: String): Resource<Unit>
    suspend fun unpairBluetooth(address: String): Resource<Unit>

    suspend fun pingHost(host: String, count: Int): Resource<VelaPingResult>
    suspend fun runSpeedTest(): Resource<VelaSpeedTest>
}
