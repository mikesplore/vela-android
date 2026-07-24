package com.template.app.core.data.local.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.template.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VelaDao {

    @Query("SELECT * FROM vela_health WHERE connectionId = :connectionId")
    fun observeHealth(connectionId: Long): Flow<VelaHealthEntity?>

    @Upsert
    suspend fun upsertHealth(health: VelaHealthEntity)

    @Query("DELETE FROM vela_health WHERE connectionId = :connectionId")
    suspend fun clearHealth(connectionId: Long)

    @Query("SELECT * FROM vela_uptime WHERE connectionId = :connectionId")
    fun observeUptime(connectionId: Long): Flow<VelaUptimeEntity?>

    @Upsert
    suspend fun upsertUptime(uptime: VelaUptimeEntity)

    @Query("SELECT * FROM NetUsageEntity WHERE connectionId = :connectionId")
    fun observeNetUsage(connectionId: Long): Flow<NetUsageEntity?>

    @Upsert
    suspend fun upsertNetUsage(netUsage: NetUsageEntity)

    @Query("SELECT * FROM vela_device WHERE connectionId = :connectionId")
    fun observeDevice(connectionId: Long): Flow<VelaDeviceEntity?>

    @Upsert
    suspend fun upsertDevice(device: VelaDeviceEntity)

    @Query("DELETE FROM vela_device WHERE connectionId = :connectionId")
    suspend fun clearDevice(connectionId: Long)

    @Query("SELECT * FROM vela_network WHERE connectionId = :connectionId")
    fun observeNetwork(connectionId: Long): Flow<VelaNetworkEntity?>

    @Upsert
    suspend fun upsertNetwork(network: VelaNetworkEntity)

    @Query("DELETE FROM vela_network WHERE connectionId = :connectionId")
    suspend fun clearNetwork(connectionId: Long)

    @Query("SELECT * FROM vela_audio WHERE connectionId = :connectionId")
    fun observeAudio(connectionId: Long): Flow<VelaAudioEntity?>

    @Upsert
    suspend fun upsertAudio(audio: VelaAudioEntity)

    @Query("SELECT * FROM vela_audio_devices WHERE connectionId = :connectionId")
    fun observeAudioDevices(connectionId: Long): Flow<List<VelaAudioDeviceEntity>>

    @Upsert
    suspend fun upsertAudioDevices(devices: List<VelaAudioDeviceEntity>)

    @Query("DELETE FROM vela_audio_devices WHERE connectionId = :connectionId")
    suspend fun clearAudioDevices(connectionId: Long)

    @Query("DELETE FROM vela_audio_devices WHERE connectionId = :connectionId AND id NOT IN (:ids)")
    suspend fun deleteAudioDevicesExcept(connectionId: Long, ids: List<String>)

    @Transaction
    suspend fun replaceAudioDevices(connectionId: Long, devices: List<VelaAudioDeviceEntity>) {
        if (devices.isEmpty()) {
            clearAudioDevices(connectionId)
        } else {
            upsertAudioDevices(devices)
            deleteAudioDevicesExcept(connectionId, devices.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_media WHERE connectionId = :connectionId")
    fun observeMedia(connectionId: Long): Flow<VelaMediaEntity?>

    @Upsert
    suspend fun upsertMedia(media: VelaMediaEntity)

    @Query("SELECT * FROM vela_processes WHERE connectionId = :connectionId AND isTopByMemory = 0 ORDER BY cpu DESC LIMIT :limit")
    fun observeProcesses(connectionId: Long, limit: Int): Flow<List<VelaProcessEntity>>

    @Query("SELECT * FROM vela_processes WHERE connectionId = :connectionId AND isTopByMemory = 1 ORDER BY mem DESC LIMIT :limit")
    fun observeProcessesByMemory(connectionId: Long, limit: Int): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes WHERE connectionId = :connectionId")
    suspend fun clearProcesses(connectionId: Long)

    @Query("DELETE FROM vela_processes WHERE connectionId = :connectionId AND isTopByMemory = 0")
    suspend fun clearCpuProcesses(connectionId: Long)

    @Query("DELETE FROM vela_processes WHERE connectionId = :connectionId AND isTopByMemory = 1")
    suspend fun clearMemoryProcesses(connectionId: Long)

    @Transaction
    suspend fun replaceCpuProcesses(connectionId: Long, processes: List<VelaProcessEntity>) {
        clearCpuProcesses(connectionId)
        if (processes.isNotEmpty()) upsertProcesses(processes)
    }

    @Transaction
    suspend fun replaceMemoryProcesses(connectionId: Long, processes: List<VelaProcessEntity>) {
        clearMemoryProcesses(connectionId)
        if (processes.isNotEmpty()) upsertProcesses(processes)
    }

    @Transaction
    suspend fun replaceProcesses(connectionId: Long, processes: List<VelaProcessEntity>) {
        if (processes.isEmpty()) {
            clearProcesses(connectionId)
        } else {
            upsertProcesses(processes)
        }
    }

    @Query("SELECT * FROM vela_disks WHERE connectionId = :connectionId")
    fun observeDisks(connectionId: Long): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks WHERE connectionId = :connectionId")
    suspend fun clearDisks(connectionId: Long)

    @Query("DELETE FROM vela_disks WHERE connectionId = :connectionId AND mountpoint NOT IN (:mountpoints)")
    suspend fun deleteDisksExcept(connectionId: Long, mountpoints: List<String>)

    @Transaction
    suspend fun replaceDisks(connectionId: Long, disks: List<VelaDiskEntity>) {
        if (disks.isEmpty()) {
            clearDisks(connectionId)
        } else {
            upsertDisks(disks)
            deleteDisksExcept(connectionId, disks.map { it.mountpoint })
        }
    }

    @Query("SELECT * FROM vela_notifications WHERE connectionId = :connectionId ORDER BY timestamp DESC")
    fun observeNotifications(connectionId: Long): Flow<List<VelaNotificationEntity>>

    @Upsert
    suspend fun upsertNotifications(notifications: List<VelaNotificationEntity>)

    @Query("DELETE FROM vela_notifications WHERE connectionId = :connectionId")
    suspend fun clearNotifications(connectionId: Long)

    @Query("DELETE FROM vela_notifications WHERE connectionId = :connectionId AND id NOT IN (:ids)")
    suspend fun deleteNotificationsExcept(connectionId: Long, ids: List<String>)

    @Transaction
    suspend fun replaceNotifications(connectionId: Long, notifications: List<VelaNotificationEntity>) {
        if (notifications.isEmpty()) {
            clearNotifications(connectionId)
        } else {
            upsertNotifications(notifications)
            deleteNotificationsExcept(connectionId, notifications.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_wifi WHERE connectionId = :connectionId")
    fun observeWifi(connectionId: Long): Flow<VelaWifiEntity?>

    @Upsert
    suspend fun upsertWifi(wifi: VelaWifiEntity)

    @Query("SELECT * FROM vela_wifi_networks WHERE connectionId = :connectionId")
    fun observeWifiNetworks(connectionId: Long): Flow<List<VelaWifiNetworkEntity>>

    @Upsert
    suspend fun upsertWifiNetworks(networks: List<VelaWifiNetworkEntity>)

    @Query("DELETE FROM vela_wifi_networks WHERE connectionId = :connectionId")
    suspend fun clearWifiNetworks(connectionId: Long)

    @Query("DELETE FROM vela_wifi_networks WHERE connectionId = :connectionId AND ssid NOT IN (:ssids)")
    suspend fun deleteWifiNetworksExcept(connectionId: Long, ssids: List<String>)

    @Transaction
    suspend fun replaceWifiNetworks(connectionId: Long, networks: List<VelaWifiNetworkEntity>) {
        if (networks.isEmpty()) {
            clearWifiNetworks(connectionId)
        } else {
            upsertWifiNetworks(networks)
            deleteWifiNetworksExcept(connectionId, networks.map { it.ssid })
        }
    }

    @Query("SELECT * FROM vela_bluetooth WHERE connectionId = :connectionId")
    fun observeBluetoothState(connectionId: Long): Flow<VelaBluetoothEntity?>

    @Upsert
    suspend fun upsertBluetoothState(state: VelaBluetoothEntity)

    @Query("SELECT * FROM vela_bluetooth_devices WHERE connectionId = :connectionId")
    fun observeBluetoothDevices(connectionId: Long): Flow<List<VelaBluetoothDeviceEntity>>

    @Upsert
    suspend fun upsertBluetoothDevices(devices: List<VelaBluetoothDeviceEntity>)

    @Query("DELETE FROM vela_bluetooth_devices WHERE connectionId = :connectionId")
    suspend fun clearBluetoothDevices(connectionId: Long)

    @Query("DELETE FROM vela_bluetooth_devices WHERE connectionId = :connectionId AND address NOT IN (:addresses)")
    suspend fun deleteBluetoothDevicesExcept(connectionId: Long, addresses: List<String>)

    @Transaction
    suspend fun replaceBluetoothDevices(connectionId: Long, devices: List<VelaBluetoothDeviceEntity>) {
        if (devices.isEmpty()) {
            clearBluetoothDevices(connectionId)
        } else {
            upsertBluetoothDevices(devices)
            deleteBluetoothDevicesExcept(connectionId, devices.map { it.address })
        }
    }

    @Query("SELECT * FROM vela_brightness WHERE connectionId = :connectionId")
    fun observeBrightness(connectionId: Long): Flow<VelaBrightnessEntity?>

    @Upsert
    suspend fun upsertBrightness(brightness: VelaBrightnessEntity)

    @Query("SELECT * FROM vela_resolution WHERE connectionId = :connectionId")
    fun observeResolution(connectionId: Long): Flow<VelaResolutionEntity?>

    @Upsert
    suspend fun upsertResolution(resolution: VelaResolutionEntity)

    @Query("SELECT * FROM vela_cpu_usage WHERE connectionId = :connectionId")
    fun observeCpuUsage(connectionId: Long): Flow<VelaCpuUsageEntity?>

    @Upsert
    suspend fun upsertCpuUsage(cpuUsage: VelaCpuUsageEntity)

    @Query("SELECT * FROM vela_ram_usage WHERE connectionId = :connectionId")
    fun observeRamUsage(connectionId: Long): Flow<VelaRamUsageEntity?>

    @Upsert
    suspend fun upsertRamUsage(ramUsage: VelaRamUsageEntity)

    @Query("SELECT * FROM vela_gpu_usage WHERE connectionId = :connectionId")
    fun observeGpuUsage(connectionId: Long): Flow<List<VelaGpuUsageEntity>>

    @Upsert
    suspend fun upsertGpuUsage(gpuUsage: List<VelaGpuUsageEntity>)

    @Query("DELETE FROM vela_gpu_usage WHERE connectionId = :connectionId")
    suspend fun clearGpuUsage(connectionId: Long)

    @Query("DELETE FROM vela_gpu_usage WHERE connectionId = :connectionId AND name NOT IN (:names)")
    suspend fun deleteGpuUsageExcept(connectionId: Long, names: List<String>)

    @Transaction
    suspend fun replaceGpuUsage(connectionId: Long, gpuUsage: List<VelaGpuUsageEntity>) {
        if (gpuUsage.isEmpty()) {
            clearGpuUsage(connectionId)
        } else {
            upsertGpuUsage(gpuUsage)
            deleteGpuUsageExcept(connectionId, gpuUsage.map { it.name })
        }
    }

    @Query("SELECT * FROM vela_disk_io WHERE connectionId = :connectionId")
    fun observeDiskIo(connectionId: Long): Flow<List<VelaDiskIoEntity>>

    @Upsert
    suspend fun upsertDiskIo(diskIo: List<VelaDiskIoEntity>)

    @Query("DELETE FROM vela_disk_io WHERE connectionId = :connectionId")
    suspend fun clearDiskIo(connectionId: Long)

    @Query("DELETE FROM vela_disk_io WHERE connectionId = :connectionId AND device NOT IN (:devices)")
    suspend fun deleteDiskIoExcept(connectionId: Long, devices: List<String>)

    @Transaction
    suspend fun replaceDiskIo(connectionId: Long, diskIo: List<VelaDiskIoEntity>) {
        if (diskIo.isEmpty()) {
            clearDiskIo(connectionId)
        } else {
            upsertDiskIo(diskIo)
            deleteDiskIoExcept(connectionId, diskIo.map { it.device })
        }
    }

    @Query("SELECT * FROM vela_network_io WHERE connectionId = :connectionId")
    fun observeNetworkIo(connectionId: Long): Flow<List<VelaNetworkIoEntity>>

    @Upsert
    suspend fun upsertNetworkIo(networkIo: List<VelaNetworkIoEntity>)

    @Query("DELETE FROM vela_network_io WHERE connectionId = :connectionId")
    suspend fun clearNetworkIo(connectionId: Long)

    @Query("DELETE FROM vela_network_io WHERE connectionId = :connectionId AND interfaceName NOT IN (:interfaces)")
    suspend fun deleteNetworkIoExcept(connectionId: Long, interfaces: List<String>)

    @Transaction
    suspend fun replaceNetworkIo(connectionId: Long, networkIo: List<VelaNetworkIoEntity>) {
        if (networkIo.isEmpty()) {
            clearNetworkIo(connectionId)
        } else {
            upsertNetworkIo(networkIo)
            deleteNetworkIoExcept(connectionId, networkIo.map { it.interfaceName })
        }
    }

    @Query("SELECT * FROM vela_temperatures WHERE connectionId = :connectionId")
    fun observeTemperatures(connectionId: Long): Flow<List<VelaTemperatureEntity>>

    @Upsert
    suspend fun upsertTemperatures(temperatures: List<VelaTemperatureEntity>)

    @Query("DELETE FROM vela_temperatures WHERE connectionId = :connectionId")
    suspend fun clearTemperatures(connectionId: Long)

    @Query("DELETE FROM vela_temperatures WHERE connectionId = :connectionId AND id NOT IN (:ids)")
    suspend fun deleteTemperaturesExcept(connectionId: Long, ids: List<String>)

    @Transaction
    suspend fun replaceTemperatures(connectionId: Long, temperatures: List<VelaTemperatureEntity>) {
        if (temperatures.isEmpty()) {
            clearTemperatures(connectionId)
        } else {
            upsertTemperatures(temperatures)
            deleteTemperaturesExcept(connectionId, temperatures.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_sensors WHERE connectionId = :connectionId")
    fun observeSensors(connectionId: Long): Flow<List<VelaSensorEntity>>

    @Upsert
    suspend fun upsertSensors(sensors: List<VelaSensorEntity>)

    @Query("DELETE FROM vela_sensors WHERE connectionId = :connectionId")
    suspend fun clearSensors(connectionId: Long)

    @Query("DELETE FROM vela_sensors WHERE connectionId = :connectionId AND name NOT IN (:names)")
    suspend fun deleteSensorsExcept(connectionId: Long, names: List<String>)

    @Transaction
    suspend fun replaceSensors(connectionId: Long, sensors: List<VelaSensorEntity>) {
        if (sensors.isEmpty()) {
            clearSensors(connectionId)
        } else {
            upsertSensors(sensors)
            deleteSensorsExcept(connectionId, sensors.map { it.name })
        }
    }

    @Query("SELECT * FROM vela_fans WHERE connectionId = :connectionId")
    fun observeFans(connectionId: Long): Flow<List<VelaFanEntity>>

    @Upsert
    suspend fun upsertFans(fans: List<VelaFanEntity>)

    @Query("DELETE FROM vela_fans WHERE connectionId = :connectionId")
    suspend fun clearFans(connectionId: Long)

    @Query("DELETE FROM vela_fans WHERE connectionId = :connectionId AND id NOT IN (:ids)")
    suspend fun deleteFansExcept(connectionId: Long, ids: List<String>)

    @Transaction
    suspend fun replaceFans(connectionId: Long, fans: List<VelaFanEntity>) {
        if (fans.isEmpty()) {
            clearFans(connectionId)
        } else {
            upsertFans(fans)
            deleteFansExcept(connectionId, fans.map { it.id })
        }
    }

    @Query("SELECT * FROM vela_battery WHERE connectionId = :connectionId")
    fun observeBattery(connectionId: Long): Flow<VelaBatteryEntity?>

    @Upsert
    suspend fun upsertBattery(battery: VelaBatteryEntity)

    @Query("SELECT * FROM vela_clipboard WHERE connectionId = :connectionId")
    fun observeClipboard(connectionId: Long): Flow<VelaClipboardEntity?>

    @Upsert
    suspend fun upsertClipboard(clipboard: VelaClipboardEntity)

    @Query("DELETE FROM vela_clipboard WHERE connectionId = :connectionId")
    suspend fun clearClipboard(connectionId: Long)

    @Query("SELECT * FROM vela_active_window WHERE connectionId = :connectionId")
    fun observeActiveWindow(connectionId: Long): Flow<VelaActiveWindowEntity?>

    @Upsert
    suspend fun upsertActiveWindow(activeWindow: VelaActiveWindowEntity)

    @Query("SELECT * FROM vela_scheduled_tasks WHERE connectionId = :connectionId ORDER BY nextRun ASC")
    fun observeScheduledTasks(connectionId: Long): Flow<List<VelaScheduledTaskEntity>>

    @Upsert
    suspend fun upsertScheduledTasks(tasks: List<VelaScheduledTaskEntity>)

    @Query("DELETE FROM vela_scheduled_tasks WHERE connectionId = :connectionId")
    suspend fun clearScheduledTasks(connectionId: Long)

    @Query("DELETE FROM vela_scheduled_tasks WHERE connectionId = :connectionId AND id = :taskId")
    suspend fun deleteScheduledTask(connectionId: Long, taskId: String)

    @Query(
        "DELETE FROM vela_scheduled_tasks WHERE connectionId = :connectionId AND id NOT IN (:keepIds)"
    )
    suspend fun deleteScheduledTasksExcept(connectionId: Long, keepIds: List<String>)

    @Transaction
    suspend fun replaceScheduledTasks(connectionId: Long, tasks: List<VelaScheduledTaskEntity>) {
        if (tasks.isEmpty()) {
            clearScheduledTasks(connectionId)
        } else {
            upsertScheduledTasks(tasks)
            deleteScheduledTasksExcept(connectionId, tasks.map { it.id })
        }
    }

    // ── Maintenance services ──

    @Query(
        """
        SELECT * FROM vela_services
        WHERE connectionId = :connectionId
          AND (
            :query = ''
            OR name LIKE '%' || :query || '%'
            OR description LIKE '%' || :query || '%'
          )
        ORDER BY
          CASE WHEN active = 'active' OR sub = 'running' THEN 0 ELSE 1 END,
          name ASC
        LIMIT :limit
        """
    )
    fun observeServices(
        connectionId: Long,
        query: String,
        limit: Int
    ): Flow<List<VelaServiceEntity>>

    @Query("SELECT COUNT(*) FROM vela_services WHERE connectionId = :connectionId")
    fun observeServiceCount(connectionId: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM vela_services
        WHERE connectionId = :connectionId
          AND (
            :query = ''
            OR name LIKE '%' || :query || '%'
            OR description LIKE '%' || :query || '%'
          )
        """
    )
    fun observeMatchedServiceCount(connectionId: Long, query: String): Flow<Int>

    @Upsert
    suspend fun upsertServices(services: List<VelaServiceEntity>)

    @Query("DELETE FROM vela_services WHERE connectionId = :connectionId")
    suspend fun clearServices(connectionId: Long)

    @Query(
        "DELETE FROM vela_services WHERE connectionId = :connectionId AND name NOT IN (:keepNames)"
    )
    suspend fun deleteServicesExcept(connectionId: Long, keepNames: List<String>)

    @Transaction
    suspend fun replaceServices(connectionId: Long, services: List<VelaServiceEntity>) {
        if (services.isEmpty()) {
            Log.d("MaintenanceRepositoryImpl", "Clearing services for connection $connectionId")
            clearServices(connectionId)
        } else {
            upsertServices(services)
            deleteServicesExcept(connectionId, services.map { it.name })
        }
    }

    @Query("SELECT * FROM vela_files WHERE connectionId = :connectionId AND parentPath = :parentPath")
    fun observeFiles(connectionId: Long, parentPath: String): Flow<List<VelaFileEntity>>

    @Upsert
    suspend fun upsertFiles(files: List<VelaFileEntity>)

    @Query("DELETE FROM vela_files WHERE connectionId = :connectionId AND parentPath = :parentPath")
    suspend fun clearFiles(connectionId: Long, parentPath: String)

    @Transaction
    suspend fun replaceFiles(connectionId: Long, parentPath: String, files: List<VelaFileEntity>) {
        // Always clear first so deletes/renames on the host drop stale Room rows
        clearFiles(connectionId, parentPath)
        if (files.isNotEmpty()) {
            upsertFiles(files)
        }
    }

    @Query("SELECT * FROM vela_config WHERE connectionId = :connectionId")
    fun observeConfig(connectionId: Long): Flow<VelaConfigEntity?>

    @Upsert
    suspend fun upsertConfig(config: VelaConfigEntity)

    @Transaction
    suspend fun deleteAllForConnection(connectionId: Long) {
        clearHealth(connectionId)
        clearDevice(connectionId)
        clearNetwork(connectionId)
        clearAudioDevices(connectionId)
        clearProcesses(connectionId)
        clearDisks(connectionId)
        clearNotifications(connectionId)
        clearWifiNetworks(connectionId)
        clearBluetoothDevices(connectionId)
        clearGpuUsage(connectionId)
        clearDiskIo(connectionId)
        clearNetworkIo(connectionId)
        clearTemperatures(connectionId)
        clearSensors(connectionId)
        clearFans(connectionId)
        clearClipboard(connectionId)
        clearScheduledTasks(connectionId)
        clearServices(connectionId)
        // Singletons + remaining tables
        clearFilesAll(connectionId)
        clearCapabilityModules(connectionId)
        clearAssistantTools(connectionId)
        clearDockerContainers(connectionId)
        clearSingletonRows(connectionId)
    }

    @Query("DELETE FROM vela_files WHERE connectionId = :connectionId")
    suspend fun clearFilesAll(connectionId: Long)

    @Query("DELETE FROM vela_uptime WHERE connectionId = :connectionId")
    suspend fun clearUptime(connectionId: Long)

    @Query("DELETE FROM NetUsageEntity WHERE connectionId = :connectionId")
    suspend fun clearNetUsage(connectionId: Long)

    @Query("DELETE FROM vela_audio WHERE connectionId = :connectionId")
    suspend fun clearAudio(connectionId: Long)

    @Query("DELETE FROM vela_media WHERE connectionId = :connectionId")
    suspend fun clearMedia(connectionId: Long)

    @Query("DELETE FROM vela_wifi WHERE connectionId = :connectionId")
    suspend fun clearWifi(connectionId: Long)

    @Query("DELETE FROM vela_bluetooth WHERE connectionId = :connectionId")
    suspend fun clearBluetooth(connectionId: Long)

    @Query("DELETE FROM vela_brightness WHERE connectionId = :connectionId")
    suspend fun clearBrightness(connectionId: Long)

    @Query("DELETE FROM vela_resolution WHERE connectionId = :connectionId")
    suspend fun clearResolution(connectionId: Long)

    @Query("DELETE FROM vela_cpu_usage WHERE connectionId = :connectionId")
    suspend fun clearCpuUsage(connectionId: Long)

    @Query("DELETE FROM vela_ram_usage WHERE connectionId = :connectionId")
    suspend fun clearRamUsage(connectionId: Long)

    @Query("DELETE FROM vela_battery WHERE connectionId = :connectionId")
    suspend fun clearBattery(connectionId: Long)

    @Query("DELETE FROM vela_active_window WHERE connectionId = :connectionId")
    suspend fun clearActiveWindow(connectionId: Long)

    @Query("DELETE FROM vela_config WHERE connectionId = :connectionId")
    suspend fun clearConfig(connectionId: Long)

    // ── Capabilities ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM vela_capabilities_meta WHERE connectionId = :connectionId")
    fun observeCapabilitiesMeta(connectionId: Long): Flow<VelaCapabilitiesMetaEntity?>

    @Query("SELECT * FROM vela_capabilities_meta WHERE connectionId = :connectionId")
    suspend fun getCapabilitiesMeta(connectionId: Long): VelaCapabilitiesMetaEntity?

    @Upsert
    suspend fun upsertCapabilitiesMeta(meta: VelaCapabilitiesMetaEntity)

    @Query("SELECT * FROM vela_capability_modules WHERE connectionId = :connectionId")
    fun observeCapabilityModules(connectionId: Long): Flow<List<VelaCapabilityModuleEntity>>

    @Query("SELECT * FROM vela_capability_modules WHERE connectionId = :connectionId")
    suspend fun getCapabilityModules(connectionId: Long): List<VelaCapabilityModuleEntity>

    @Upsert
    suspend fun upsertCapabilityModules(modules: List<VelaCapabilityModuleEntity>)

    @Query("DELETE FROM vela_capability_modules WHERE connectionId = :connectionId")
    suspend fun clearCapabilityModules(connectionId: Long)

    @Query("DELETE FROM vela_capability_modules WHERE connectionId = :connectionId AND moduleKey NOT IN (:keys)")
    suspend fun deleteCapabilityModulesExcept(connectionId: Long, keys: List<String>)

    @Query("SELECT * FROM vela_assistant_tools WHERE connectionId = :connectionId AND available = 1")
    fun observeAvailableAssistantTools(connectionId: Long): Flow<List<VelaAssistantToolEntity>>

    @Query("SELECT * FROM vela_assistant_tools WHERE connectionId = :connectionId")
    suspend fun getAssistantTools(connectionId: Long): List<VelaAssistantToolEntity>

    @Upsert
    suspend fun upsertAssistantTools(tools: List<VelaAssistantToolEntity>)

    @Query("DELETE FROM vela_assistant_tools WHERE connectionId = :connectionId")
    suspend fun clearAssistantTools(connectionId: Long)

    @Query("DELETE FROM vela_assistant_tools WHERE connectionId = :connectionId AND toolName NOT IN (:names)")
    suspend fun deleteAssistantToolsExcept(connectionId: Long, names: List<String>)

    @Query("DELETE FROM vela_capabilities_meta WHERE connectionId = :connectionId")
    suspend fun clearCapabilitiesMeta(connectionId: Long)

    @Transaction
    suspend fun replaceCapabilities(
        connectionId: Long,
        meta: VelaCapabilitiesMetaEntity,
        modules: List<VelaCapabilityModuleEntity>,
        tools: List<VelaAssistantToolEntity>
    ) {
        upsertCapabilitiesMeta(meta)
        if (modules.isEmpty()) {
            clearCapabilityModules(connectionId)
        } else {
            upsertCapabilityModules(modules)
            deleteCapabilityModulesExcept(connectionId, modules.map { it.moduleKey })
        }
        if (tools.isEmpty()) {
            clearAssistantTools(connectionId)
        } else {
            upsertAssistantTools(tools)
            deleteAssistantToolsExcept(connectionId, tools.map { it.toolName })
        }
    }

    // ── Docker ────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM vela_docker_info WHERE connectionId = :connectionId")
    fun observeDockerInfo(connectionId: Long): Flow<VelaDockerInfoEntity?>

    @Upsert
    suspend fun upsertDockerInfo(info: VelaDockerInfoEntity)

    @Query("DELETE FROM vela_docker_info WHERE connectionId = :connectionId")
    suspend fun clearDockerInfo(connectionId: Long)

    @Query("SELECT * FROM vela_docker_containers WHERE connectionId = :connectionId ORDER BY name ASC")
    fun observeDockerContainers(connectionId: Long): Flow<List<VelaDockerContainerEntity>>

    @Upsert
    suspend fun upsertDockerContainers(containers: List<VelaDockerContainerEntity>)

    @Query("DELETE FROM vela_docker_containers WHERE connectionId = :connectionId")
    suspend fun clearDockerContainers(connectionId: Long)

    @Query("DELETE FROM vela_docker_containers WHERE connectionId = :connectionId AND id NOT IN (:ids)")
    suspend fun deleteDockerContainersExcept(connectionId: Long, ids: List<String>)

    @Transaction
    suspend fun replaceDockerContainers(connectionId: Long, containers: List<VelaDockerContainerEntity>) {
        if (containers.isEmpty()) {
            clearDockerContainers(connectionId)
        } else {
            upsertDockerContainers(containers)
            deleteDockerContainersExcept(connectionId, containers.map { it.id })
        }
    }

    @Transaction
    suspend fun clearSingletonRows(connectionId: Long) {
        clearUptime(connectionId)
        clearNetUsage(connectionId)
        clearAudio(connectionId)
        clearMedia(connectionId)
        clearWifi(connectionId)
        clearBluetooth(connectionId)
        clearBrightness(connectionId)
        clearResolution(connectionId)
        clearCpuUsage(connectionId)
        clearRamUsage(connectionId)
        clearBattery(connectionId)
        clearActiveWindow(connectionId)
        clearConfig(connectionId)
        clearDockerInfo(connectionId)
        clearCapabilitiesMeta(connectionId)
    }
}
