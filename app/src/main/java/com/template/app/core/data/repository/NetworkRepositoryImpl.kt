package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : NetworkRepository {

    override fun observeNetwork(): Flow<VelaNetworkInfo?> =
        velaDao.observeNetwork().map { it?.toDomain() }

    override fun observeWifi(): Flow<VelaWifiStatus?> =
        combine(
            velaDao.observeWifi(),
            velaDao.observeWifiNetworks()
        ) { status, networks ->
            val actualStatus = status ?: VelaWifiEntity(connected = false, ssid = null, device = null, signal = null, isEnabled = true)
            actualStatus.toDomain(networks.map { it.toDomain() })
        }

    override fun observeBluetooth(): Flow<VelaBluetoothStatus?> =
        combine(
            velaDao.observeBluetoothState(),
            velaDao.observeBluetoothDevices()
        ) { state, devices ->
            val actualState = state ?: VelaBluetoothEntity(isEnabled = true)
            actualState.toDomain(
                connected = devices.filter { it.isConnected }.map { it.toDomain() },
                paired = devices.filter { it.isPaired }.map { it.toDomain() }
            )
        }

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkIp()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getNetworkLocation(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkLocation()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp,
            location = response.location?.let {
                VelaLocation(
                    status = it.status,
                    country = it.country,
                    region = it.region,
                    city = it.city,
                    zip = it.zip,
                    timezone = it.timezone,
                    isp = it.isp,
                    lat = it.lat,
                    lon = it.lon
                )
            }
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    private fun WifiStatusResponse.toDomainModel(): VelaWifiStatus {
        return VelaWifiStatus(
            connected = connected ?: false,
            ssid = ssid,
            device = device,
            signal = signal,
            isEnabled = true, 
            availableNetworks = networks?.map {
                VelaWifiNetwork(
                    ssid = it.ssid ?: "Unknown",
                    security = it.security,
                    signal = it.signal,
                    isActive = it.active ?: false
                )
            } ?: emptyList()
        )
    }

    override suspend fun getWifiStatus(): Resource<VelaWifiStatus> = safeApiCall {
        val res = apiService.getWifiStatus()
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        velaDao.replaceWifiNetworks(domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(it) })
        domain
    }

    override suspend fun getWifiList(): Resource<VelaWifiStatus> = safeApiCall {
        val res = apiService.getWifiList()
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        velaDao.replaceWifiNetworks(domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(it) })
        domain
    }

    override suspend fun connectWifi(ssid: String, password: String?): Resource<VelaWifiStatus> = safeApiCall {
        val res = apiService.connectWifi(WifiConnectRequest(ssid, password))
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        velaDao.replaceWifiNetworks(domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(it) })
        domain
    }

    override suspend fun disconnectWifi(): Resource<VelaNetworkInfo> = safeApiCall {
        val res = apiService.disconnectWifi()
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        val currentWifi = VelaWifiStatus(false, null, null, null, true)
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(currentWifi))
        domain
    }

    override suspend fun toggleWifi(enabled: Boolean): Resource<VelaNetworkInfo> = safeApiCall {
        val res = apiService.toggleWifi(WifiToggleRequest(enabled))
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        val currentWifi = VelaWifiStatus(false, null, null, null, enabled)
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(currentWifi))
        domain
    }

    override suspend fun toggleBluetooth(enabled: Boolean): Resource<VelaNetworkInfo> = safeApiCall {
        val res = apiService.toggleBluetooth(BluetoothToggleRequest(enabled))
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        velaDao.upsertBluetoothState(VelaBluetoothEntity(isEnabled = enabled))
        domain
    }

    override suspend fun getBluetoothDevices(): Resource<VelaBluetoothStatus> = safeApiCall {
        val response = apiService.getBluetoothDevices()
        
        val connected = response.connectedDevices?.map {
            VelaBluetoothDevice(it.address ?: "", it.name ?: "Unknown", true, it.paired ?: true)
        } ?: emptyList()

        val paired = response.pairedDevices?.map {
            VelaBluetoothDevice(it.address ?: "", it.name ?: "Unknown", it.connected ?: false, true)
        } ?: emptyList()

        val allDevices = (connected + paired).distinctBy { it.address }
        
        velaDao.replaceBluetoothDevices(allDevices.map { VelaBluetoothDeviceEntity.fromDomain(it) })
        
        // Also ensure the base bluetooth state entity exists so observers trigger
        velaDao.upsertBluetoothState(VelaBluetoothEntity(isEnabled = true))
        
        VelaBluetoothStatus(connected, paired, true)
    }

    override suspend fun pairBluetooth(address: String): Resource<Unit> = safeApiCall {
        apiService.pairBluetooth(BluetoothDeviceRequest(address))
        Unit
    }

    override suspend fun connectBluetooth(address: String): Resource<Unit> = safeApiCall {
        apiService.connectBluetooth(BluetoothDeviceRequest(address))
        Unit
    }

    override suspend fun disconnectBluetooth(address: String): Resource<Unit> = safeApiCall {
        apiService.disconnectBluetooth(BluetoothDeviceRequest(address))
        Unit
    }

    override suspend fun unpairBluetooth(address: String): Resource<Unit> = safeApiCall {
        apiService.unpairBluetooth(BluetoothDeviceRequest(address))
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
}
