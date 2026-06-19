package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun observeNetwork(): Flow<VelaNetworkInfo?>
    fun observeWifi(): Flow<VelaWifiStatus?>
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
}
