package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MonitorRepository {
    fun observeCpuUsage(): Flow<VelaCpuUsage?>
    fun observeRamUsage(): Flow<VelaRamUsage?>
    fun observeGpuUsage(): Flow<List<VelaGpuUsage>>
    fun observeDiskIo(): Flow<List<VelaDiskIo>>
    fun observeNetworkIo(): Flow<List<VelaNetworkIo>>
    fun observeTemperatures(): Flow<List<VelaTemperatureInfo>>
    fun observeFans(): Flow<List<VelaFanInfo>>
    fun observeSensors(): Flow<List<VelaSensorInfo>>
    fun observeBattery(): Flow<VelaBatteryStatus?>
    fun observeTopProcessesByCpu(limit: Int = 5): Flow<List<VelaProcess>>
    fun observeTopProcessesByMemory(limit: Int = 5): Flow<List<VelaProcess>>
    suspend fun getCpuUsage(): Resource<VelaCpuUsage>
    suspend fun getRamUsage(): Resource<VelaRamUsage>
    suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot>

}
