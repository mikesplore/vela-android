package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaNetworkEntity
import com.template.app.core.data.local.entities.VelaWifiEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : NetworkRepository {

    override fun observeWifi(): Flow<VelaWifiStatus?> =
        velaDao.observeWifi().map { it?.toDomain() }

    override fun observeNetwork(): Flow<VelaNetworkInfo?> =
        velaDao.observeNetwork().map { it?.toDomain() }

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkIp()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = response.interfaceName ?: ""
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getNetworkLocation(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkLocation()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = "",
            location = response.location?.let {
                VelaLocation(it.country, it.city, it.lat, it.lon)
            }
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getWifiStatus(): Resource<VelaWifiStatus> = safeApiCall {
        val res = apiService.getWifiStatus()
        val domain = VelaWifiStatus(
            connected = res.connected ?: false,
            ssid = res.ssid,
            signal = res.signal,
            availableNetworks = res.networks?.map { VelaWifiNetwork(it.ssid ?: "Unknown", it.signal ?: 0) } ?: emptyList()
        )
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        domain
    }

    override suspend fun getWifiList(): Resource<List<VelaWifiNetwork>> = safeApiCall {
        apiService.getWifiList().networks?.map {
            VelaWifiNetwork(it.ssid ?: "Unknown", it.signal ?: 0)
        } ?: emptyList()
    }

    override suspend fun connectWifi(ssid: String, password: String): Resource<Unit> = safeApiCall {
        apiService.connectWifi(WifiConnectRequest(ssid, password))
        Unit
    }

    override suspend fun disconnectWifi(): Resource<Unit> = safeApiCall {
        apiService.disconnectWifi()
        Unit
    }

    override suspend fun toggleWifi(enabled: Boolean): Resource<Unit> = safeApiCall {
        apiService.toggleWifi(WifiToggleRequest(enabled))
        Unit
    }

    override suspend fun pingHost(host: String, count: Int): Resource<VelaPingResult> = safeApiCall {
        val res = apiService.pingHost(PingHostRequest(host, count))
        VelaPingResult(
            host = res.host ?: host,
            lossPercent = res.packetLoss ?: 0.0,
            avgRttMs = res.avgRttMs ?: 0.0,
            transmitted = res.packetsTransmitted ?: 0,
            received = res.packetsReceived ?: 0
        )
    }

    override suspend fun runSpeedTest(): Resource<VelaSpeedTest> = safeApiCall {
        val res = apiService.speedTest()
        VelaSpeedTest(
            downloadMbps = res.downloadMbps ?: 0.0,
            uploadMbps = res.uploadMbps ?: 0.0,
            pingMs = res.pingMs ?: 0.0
        )
    }

    override suspend fun getBluetoothDevices(): Resource<List<VelaBluetoothDevice>> = safeApiCall {
        apiService.getBluetoothDevices().devices?.map {
            VelaBluetoothDevice(it.id ?: "", it.name ?: "Unknown", it.paired ?: false)
        } ?: return@safeApiCall emptyList()
    }

    override suspend fun pairBluetooth(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.pairBluetooth(BluetoothDeviceRequest(deviceId))
        Unit
    }

    override suspend fun unpairBluetooth(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.unpairBluetooth(BluetoothDeviceRequest(deviceId))
        Unit
    }
}
