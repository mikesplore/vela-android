package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scopedNullable
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
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : NetworkRepository {

    override fun observeNetwork(): Flow<VelaNetworkInfo?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeNetwork(id).map { it?.toDomain() }
        }

    override fun observeWifi(): Flow<VelaWifiStatus?> =
        activeConnection.scopedNullable { id ->
            combine(
                velaDao.observeWifi(id),
                velaDao.observeWifiNetworks(id)
            ) { status, networks ->
                val actualStatus = status ?: VelaWifiEntity(
                    connectionId = id,
                    connected = false,
                    ssid = null,
                    device = null,
                    signal = null,
                    isEnabled = true
                )
                actualStatus.toDomain(networks.map { it.toDomain() })
            }
        }

    override fun observeBluetooth(): Flow<VelaBluetoothStatus?> =
        activeConnection.scopedNullable { id ->
            combine(
                velaDao.observeBluetoothState(id),
                velaDao.observeBluetoothDevices(id)
            ) { state, devices ->
                val actualState = state ?: VelaBluetoothEntity(connectionId = id, isEnabled = true)
                actualState.toDomain(
                    connected = devices.filter { it.isConnected }.map { it.toDomain() },
                    paired = devices.filter { it.isPaired }.map { it.toDomain() }
                )
            }
        }

    override fun observeNetUsage(): Flow<NetUsage?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeNetUsage(id).map { it?.toDomain() }
        }

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.getNetworkIp()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun getNetworkLocation(): Resource<VelaNetworkInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
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
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(connectionId, domain))
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
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getWifiStatus()
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(connectionId, domain))
        velaDao.replaceWifiNetworks(connectionId, domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(connectionId, it) })
        domain
    }

    override suspend fun getWifiList(): Resource<VelaWifiStatus> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getWifiList()
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(connectionId, domain))
        velaDao.replaceWifiNetworks(connectionId, domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(connectionId, it) })
        domain
    }

    override suspend fun connectWifi(ssid: String, password: String?): Resource<VelaWifiStatus> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.connectWifi(WifiConnectRequest(ssid, password))
        val domain = res.toDomainModel()
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(connectionId, domain))
        velaDao.replaceWifiNetworks(connectionId, domain.availableNetworks.map { VelaWifiNetworkEntity.fromDomain(connectionId, it) })
        domain
    }

    override suspend fun disconnectWifi(): Resource<VelaNetworkInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.disconnectWifi()
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(connectionId, domain))
        val currentWifi = VelaWifiStatus(false, null, null, null, true)
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(connectionId, currentWifi))
        domain
    }

    override suspend fun toggleWifi(enabled: Boolean): Resource<VelaNetworkInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.toggleWifi(WifiToggleRequest(enabled))
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(connectionId, domain))
        val currentWifi = VelaWifiStatus(false, null, null, null, enabled)
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(connectionId, currentWifi))
        domain
    }

    override suspend fun toggleBluetooth(enabled: Boolean): Resource<VelaNetworkInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.toggleBluetooth(BluetoothToggleRequest(enabled))
        val domain = VelaNetworkInfo(
            localIp = res.localIp ?: "",
            publicIp = res.publicIp,
            location = null
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(connectionId, domain))
        velaDao.upsertBluetoothState(VelaBluetoothEntity(connectionId = connectionId, isEnabled = enabled))
        domain
    }

    override suspend fun getBluetoothDevices(): Resource<VelaBluetoothStatus> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.getBluetoothDevices()

        val connected = response.connectedDevices?.map {
            VelaBluetoothDevice(it.address ?: "", it.name ?: "Unknown", true, it.paired ?: true)
        } ?: emptyList()

        val paired = response.pairedDevices?.map {
            VelaBluetoothDevice(it.address ?: "", it.name ?: "Unknown", it.connected ?: false, true)
        } ?: emptyList()

        val allDevices = (connected + paired).distinctBy { it.address }

        velaDao.replaceBluetoothDevices(connectionId, allDevices.map { VelaBluetoothDeviceEntity.fromDomain(connectionId, it) })
        velaDao.upsertBluetoothState(VelaBluetoothEntity(connectionId = connectionId, isEnabled = true))

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

    override suspend fun getNetworkUsage(period: String): Resource<NetUsage> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getNetworkUsage(period)
        val domain = NetUsage(
            interfaceName = res.interfaceName,
            period = res.period,
            receivedBytes = res.receivedBytes,
            transmittedBytes = res.transmittedBytes,
            received = res.received,
            transmitted = res.transmitted
        )
        velaDao.upsertNetUsage(NetUsageEntity.fromDomain(connectionId, domain))
        domain
    }
}
