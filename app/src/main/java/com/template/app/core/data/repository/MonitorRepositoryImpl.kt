package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ProcessItem
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
    private val velaDao: VelaDao
) : MonitorRepository {

    override fun observeCpuUsage(): Flow<VelaCpuUsage?> =
        velaDao.observeCpuUsage().map { it?.toDomain() }

    override fun observeRamUsage(): Flow<VelaRamUsage?> =
        velaDao.observeRamUsage().map { it?.toDomain() }

    override fun observeGpuUsage(): Flow<List<VelaGpuUsage>> =
        velaDao.observeGpuUsage().map { list -> list.map { it.toDomain() } }

    override fun observeDiskIo(): Flow<List<VelaDiskIo>> =
        velaDao.observeDiskIo().map { list -> list.map { it.toDomain() } }

    override fun observeNetworkIo(): Flow<List<VelaNetworkIo>> =
        velaDao.observeNetworkIo().map { list -> list.map { it.toDomain() } }

    override fun observeTemperatures(): Flow<List<VelaTemperatureInfo>> =
        velaDao.observeTemperatures().map { list -> list.map { it.toDomain() } }

    override fun observeFans(): Flow<List<VelaFanInfo>> =
        velaDao.observeFans().map { list -> list.map { it.toDomain() } }

    override fun observeSensors(): Flow<List<VelaSensorInfo>> =
        velaDao.observeSensors().map { list -> list.map { it.toDomain() } }

    override fun observeBattery(): Flow<VelaBatteryStatus?> =
        velaDao.observeBattery().map { it?.toDomain() }

    override fun observeTopProcessesByCpu(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcesses(limit).map { list -> list.map { it.toDomain() } }

    override fun observeTopProcessesByMemory(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcessesByMemory(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getCpuUsage(): Resource<VelaCpuUsage> = safeApiCall {
        val res = apiService.getMonitorCpu()
        val domain = VelaCpuUsage(res.overall ?: 0.0, res.perCore ?: emptyList())
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(domain))
        domain
    }

    override suspend fun getRamUsage(): Resource<VelaRamUsage> = safeApiCall {
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
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(domain))
        domain
    }

    override suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot> = safeApiCall {
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
        val cpuProcDomains = res.processes?.topByCpu?.map { it.toDomain() } ?: emptyList()
        val memProcDomains = res.processes?.topByMemory?.map { it.toDomain() } ?: emptyList()

        // generic sensors could be added here if available in snapshot
        val sensorDomains = emptyList<VelaSensorInfo>()

        // Sync to Room
        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(cpuDomain))
        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(ramDomain))
        velaDao.replaceGpuUsage(gpuDomains.map { VelaGpuUsageEntity.fromDomain(it) })
        velaDao.replaceDiskIo(diskIoDomains.map { VelaDiskIoEntity.fromDomain(it) })
        velaDao.replaceNetworkIo(networkIoDomains.map { VelaNetworkIoEntity.fromDomain(it) })
        velaDao.replaceTemperatures(tempDomains.map { VelaTemperatureEntity.fromDomain(it) })
        velaDao.replaceFans(fanDomains.map { VelaFanEntity.fromDomain(it) })
        velaDao.replaceSensors(sensorDomains.map { VelaSensorEntity.fromDomain(it) })

        if (batteryDomain != null) velaDao.upsertBattery(VelaBatteryEntity.fromDomain(batteryDomain))
        velaDao.replaceCpuProcesses(cpuProcDomains.map {
            VelaProcessEntity.fromDomain(
                it,
                isTopByMemory = false
            )
        })
        velaDao.replaceMemoryProcesses(memProcDomains.map {
            VelaProcessEntity.fromDomain(
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
            sensors = sensorDomains
        )
    }

    private fun ProcessItem.toDomain() = VelaProcess(
        pid = pid ?: 0,
        name = name ?: "Unknown",
        cpu = cpu ?: 0.0,
        mem = mem ?: 0.0,
        username = username,
        memoryRss = memRss
    )
}
