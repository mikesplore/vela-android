package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ProcessItem
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.MonitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : MonitorRepository {

    override fun observeCpuUsage(): Flow<VelaCpuUsage?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeCpuUsage(id).map { it?.toDomain() }
        }

    override fun observeRamUsage(): Flow<VelaRamUsage?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeRamUsage(id).map { it?.toDomain() }
        }

    override fun observeGpuUsage(): Flow<List<VelaGpuUsage>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeGpuUsage(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeDiskIo(): Flow<List<VelaDiskIo>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeDiskIo(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeNetworkIo(): Flow<List<VelaNetworkIo>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeNetworkIo(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeTemperatures(): Flow<List<VelaTemperatureInfo>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeTemperatures(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeFans(): Flow<List<VelaFanInfo>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeFans(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeSensors(): Flow<List<VelaSensorInfo>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeSensors(id).map { list -> list.map { it.toDomain() } }
        }

    override fun observeBattery(): Flow<VelaBatteryStatus?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeBattery(id).map { it?.toDomain() }
        }

    override fun observeTopProcessesByCpu(limit: Int): Flow<List<VelaProcess>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeProcesses(id, limit).map { list -> list.map { it.toDomain() } }
        }

    override fun observeTopProcessesByMemory(limit: Int): Flow<List<VelaProcess>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeProcessesByMemory(id, limit).map { list -> list.map { it.toDomain() } }
        }

    override fun observeUptime(): Flow<VelaUptime?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeUptime(id).map { it?.toDomain() }
        }

    override suspend fun getUptime(): Resource<VelaUptime> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getUptime()
        val domain = VelaUptime(
            seconds = res.seconds,
            minutes = res.minutes,
            hours = res.hours,
            days = res.days,
            weeks = res.weeks,
            months = res.months,
            years = res.years,
            formatted = res.formatted
        )
        velaDao.upsertUptime(VelaUptimeEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun getCpuUsage(): Resource<VelaCpuUsage> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getMonitorCpu()
        val domain = VelaCpuUsage(res.overall ?: 0.0, res.perCore ?: emptyList())
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun getRamUsage(): Resource<VelaRamUsage> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getMonitorRam()
        val domain = VelaRamUsage(
            total = res.total ?: 0,
            available = res.available ?: 0,
            used = res.used ?: 0,
            percent = res.percent ?: 0.0,
            swapTotal = res.swapTotal ?: 0,
            swapUsed = res.swapUsed ?: 0,
            swapFree = res.swapFree ?: 0,
            swapPercent = res.swapPercent ?: 0.0
        )
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun getDiskIo(): Resource<List<VelaDiskIo>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val domains = apiService.getMonitorDiskIo().map {
            VelaDiskIo(
                device = it.device ?: "unknown",
                readBytesPerSec = it.readBytesPerSec ?: 0.0,
                writeBytesPerSec = it.writeBytesPerSec ?: 0.0
            )
        }
        velaDao.replaceDiskIo(connectionId, domains.map { VelaDiskIoEntity.fromDomain(connectionId, it) })
        domains
    }

    override suspend fun getNetworkIo(): Resource<List<VelaNetworkIo>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val domains = apiService.getMonitorNetworkIo().map {
            VelaNetworkIo(
                interfaceName = it.interfaceName ?: "unknown",
                bytesSentPerSec = it.bytesSentPerSec ?: 0.0,
                bytesRecvPerSec = it.bytesRecvPerSec ?: 0.0
            )
        }
        velaDao.replaceNetworkIo(connectionId, domains.map { VelaNetworkIoEntity.fromDomain(connectionId, it) })
        domains
    }

    override suspend fun getMonitorProcesses(): Resource<Pair<List<VelaProcess>, List<VelaProcess>>> =
        safeApiCall {
            val connectionId = activeConnection.requireActiveId()
            val res = apiService.getMonitorProcesses()
            val cpuDomains = res.cpuProcesses().map { it.toDomain() }
            val memDomains = res.memoryProcesses().map { it.toDomain() }
            velaDao.replaceCpuProcesses(
                connectionId,
                cpuDomains.map { VelaProcessEntity.fromDomain(connectionId, it, isTopByMemory = false) }
            )
            velaDao.replaceMemoryProcesses(
                connectionId,
                memDomains.map { VelaProcessEntity.fromDomain(connectionId, it, isTopByMemory = true) }
            )
            cpuDomains to memDomains
        }

    override suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getMonitorSnapshot()

        val cpuDomain = VelaCpuUsage(res.cpu?.overall ?: 0.0, res.cpu?.perCore ?: emptyList())
        val ramDomain = VelaRamUsage(
            total = res.ram?.total ?: 0,
            available = res.ram?.available ?: 0,
            used = res.ram?.used ?: 0,
            percent = res.ram?.percent ?: 0.0,
            swapTotal = res.ram?.swapTotal ?: 0,
            swapUsed = res.ram?.swapUsed ?: 0,
            swapFree = res.ram?.swapFree ?: 0,
            swapPercent = res.ram?.swapPercent ?: 0.0
        )
        val gpuDomains = res.gpu?.map {
            VelaGpuUsage(
                it.name,
                it.usagePercent ?: 0.0,
                it.vramTotal ?: 0,
                it.vramUsed ?: 0,
                it.vramPercent ?: 0.0
            )
        } ?: emptyList()
        val diskIoDomains = res.diskIo?.map {
            VelaDiskIo(
                it.device ?: "unknown",
                it.readBytesPerSec ?: 0.0,
                it.writeBytesPerSec ?: 0.0
            )
        } ?: emptyList()
        val networkIoDomains = res.networkIo?.map {
            VelaNetworkIo(
                it.interfaceName ?: "unknown",
                it.bytesSentPerSec ?: 0.0,
                it.bytesRecvPerSec ?: 0.0
            )
        } ?: emptyList()
        val tempDomains = res.temperatures?.map {
            VelaTemperatureInfo(
                it.sensor ?: "unknown",
                it.label ?: "",
                it.current ?: 0.0,
                it.high,
                it.critical
            )
        } ?: emptyList()
        val fanDomains = res.fans?.mapIndexed { index, it ->
            VelaFanInfo(it.sensor ?: "unknown", it.speedRpm ?: 0, index)
        } ?: emptyList()
        val batteryDomain = res.battery?.let {
            VelaBatteryStatus(it.percent ?: 0.0, it.pluggedIn ?: false, it.secsLeft)
        }
        val cpuProcDomains = res.processes?.cpuProcesses()?.map { it.toDomain() } ?: emptyList()
        val memProcDomains = res.processes?.memoryProcesses()?.map { it.toDomain() } ?: emptyList()

        // Sync to Room
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(connectionId, cpuDomain))
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(connectionId, ramDomain))
        velaDao.replaceGpuUsage(connectionId, gpuDomains.map { VelaGpuUsageEntity.fromDomain(connectionId, it) })
        velaDao.replaceDiskIo(connectionId, diskIoDomains.map { VelaDiskIoEntity.fromDomain(connectionId, it) })
        velaDao.replaceNetworkIo(connectionId, networkIoDomains.map { VelaNetworkIoEntity.fromDomain(connectionId, it) })
        velaDao.replaceTemperatures(connectionId, tempDomains.map { VelaTemperatureEntity.fromDomain(connectionId, it) })
        velaDao.replaceFans(connectionId, fanDomains.map { VelaFanEntity.fromDomain(connectionId, it) })

        if (batteryDomain != null) velaDao.upsertBattery(VelaBatteryEntity.fromDomain(connectionId, batteryDomain))
        velaDao.replaceCpuProcesses(connectionId, cpuProcDomains.map {
            VelaProcessEntity.fromDomain(
                connectionId,
                it,
                isTopByMemory = false
            )
        })
        velaDao.replaceMemoryProcesses(connectionId, memProcDomains.map {
            VelaProcessEntity.fromDomain(
                connectionId,
                it,
                isTopByMemory = true
            )
        })

        VelaMonitorSnapshot(
            cpu = cpuDomain,
            ram = ramDomain,
            gpu = gpuDomains,
            diskIo = diskIoDomains,
            networkIo = networkIoDomains,
            temperatures = tempDomains,
            fans = fanDomains,
            battery = batteryDomain,
            topProcessesByCpu = cpuProcDomains,
            topProcessesByMemory = memProcDomains,
            sensors = emptyList()
        )
    }

    private fun ProcessItem.toDomain() = VelaProcess(
        pid = pid ?: 0,
        name = name ?: "Unknown",
        cpu = cpu ?: 0.0,
        mem = mem ?: 0.0,
        username = username,
        memoryRss = memoryRssBytes()
    )
}
