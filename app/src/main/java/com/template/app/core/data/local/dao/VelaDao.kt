package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.template.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VelaDao {

    @Query("SELECT * FROM vela_health WHERE id = 0")
    fun observeHealth(): Flow<VelaHealthEntity?>

    @Upsert
    suspend fun upsertHealth(health: VelaHealthEntity)

    @Query("DELETE FROM vela_health")
    suspend fun clearHealth()

    @Query("SELECT * FROM vela_network WHERE id = 0")
    fun observeNetwork(): Flow<VelaNetworkEntity?>

    @Upsert
    suspend fun upsertNetwork(network: VelaNetworkEntity)

    @Query("DELETE FROM vela_network")
    suspend fun clearNetwork()

    @Query("SELECT * FROM vela_audio WHERE id = 0")
    fun observeAudio(): Flow<VelaAudioEntity?>

    @Upsert
    suspend fun upsertAudio(audio: VelaAudioEntity)

    @Query("SELECT * FROM vela_audio_devices")
    fun observeAudioDevices(): Flow<List<VelaAudioDeviceEntity>>

    @Upsert
    suspend fun upsertAudioDevices(devices: List<VelaAudioDeviceEntity>)

    @Query("DELETE FROM vela_audio_devices")
    suspend fun clearAudioDevices()

    @Transaction
    suspend fun replaceAudioDevices(devices: List<VelaAudioDeviceEntity>) {
        clearAudioDevices()
        if (devices.isNotEmpty()) {
            upsertAudioDevices(devices)
        }
    }

    @Query("SELECT * FROM vela_media WHERE id = 0")
    fun observeMedia(): Flow<VelaMediaEntity?>

    @Upsert
    suspend fun upsertMedia(media: VelaMediaEntity)

    @Query("SELECT * FROM vela_processes WHERE isTopByMemory = 0 ORDER BY cpu DESC LIMIT :limit")
    fun observeProcesses(limit: Int): Flow<List<VelaProcessEntity>>

    @Query("SELECT * FROM vela_processes WHERE isTopByMemory = 1 ORDER BY mem DESC LIMIT :limit")
    fun observeProcessesByMemory(limit: Int): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes")
    suspend fun clearProcesses()

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 0")
    suspend fun clearCpuProcesses()

    @Query("DELETE FROM vela_processes WHERE isTopByMemory = 1")
    suspend fun clearMemoryProcesses()

    @Transaction
    suspend fun replaceCpuProcesses(processes: List<VelaProcessEntity>) {
        clearCpuProcesses()
        if (processes.isNotEmpty()) {
            upsertProcesses(processes)
        }
    }

    @Transaction
    suspend fun replaceMemoryProcesses(processes: List<VelaProcessEntity>) {
        clearMemoryProcesses()
        if (processes.isNotEmpty()) {
            upsertProcesses(processes)
        }
    }

    @Transaction
    suspend fun replaceProcesses(processes: List<VelaProcessEntity>) {
        clearProcesses()
        if (processes.isNotEmpty()) {
            upsertProcesses(processes)
        }
    }

    @Query("SELECT * FROM vela_disks")
    fun observeDisks(): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks")
    suspend fun clearDisks()

    @Transaction
    suspend fun replaceDisks(disks: List<VelaDiskEntity>) {
        clearDisks()
        if (disks.isNotEmpty()) {
            upsertDisks(disks)
        }
    }

    @Query("SELECT * FROM vela_notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<VelaNotificationEntity>>

    @Upsert
    suspend fun upsertNotifications(notifications: List<VelaNotificationEntity>)

    @Query("DELETE FROM vela_notifications")
    suspend fun clearNotifications()

    @Transaction
    suspend fun replaceNotifications(notifications: List<VelaNotificationEntity>) {
        clearNotifications()
        if (notifications.isNotEmpty()) {
            upsertNotifications(notifications)
        }
    }

    // --- Network / Wifi / Bluetooth ---

    @Query("SELECT * FROM vela_wifi WHERE id = 0")
    fun observeWifi(): Flow<VelaWifiEntity?>

    @Upsert
    suspend fun upsertWifi(wifi: VelaWifiEntity)

    @Query("SELECT * FROM vela_wifi_networks")
    fun observeWifiNetworks(): Flow<List<VelaWifiNetworkEntity>>

    @Upsert
    suspend fun upsertWifiNetworks(networks: List<VelaWifiNetworkEntity>)

    @Query("DELETE FROM vela_wifi_networks")
    suspend fun clearWifiNetworks()

    @Transaction
    suspend fun replaceWifiNetworks(networks: List<VelaWifiNetworkEntity>) {
        clearWifiNetworks()
        if (networks.isNotEmpty()) {
            upsertWifiNetworks(networks)
        }
    }

    @Query("SELECT * FROM vela_bluetooth WHERE id = 0")
    fun observeBluetoothState(): Flow<VelaBluetoothEntity?>

    @Upsert
    suspend fun upsertBluetoothState(state: VelaBluetoothEntity)

    @Query("SELECT * FROM vela_bluetooth_devices")
    fun observeBluetoothDevices(): Flow<List<VelaBluetoothDeviceEntity>>

    @Upsert
    suspend fun upsertBluetoothDevices(devices: List<VelaBluetoothDeviceEntity>)

    @Query("DELETE FROM vela_bluetooth_devices")
    suspend fun clearBluetoothDevices()

    @Transaction
    suspend fun replaceBluetoothDevices(devices: List<VelaBluetoothDeviceEntity>) {
        clearBluetoothDevices()
        if (devices.isNotEmpty()) {
            upsertBluetoothDevices(devices)
        }
    }

    @Query("SELECT * FROM vela_brightness WHERE id = 0")
    fun observeBrightness(): Flow<VelaBrightnessEntity?>

    @Upsert
    suspend fun upsertBrightness(brightness: VelaBrightnessEntity)

    @Query("SELECT * FROM vela_resolution WHERE id = 0")
    fun observeResolution(): Flow<VelaResolutionEntity?>

    @Upsert
    suspend fun upsertResolution(resolution: VelaResolutionEntity)

    @Query("SELECT * FROM vela_cpu_usage WHERE id = 0")
    fun observeCpuUsage(): Flow<VelaCpuUsageEntity?>

    @Upsert
    suspend fun upsertCpuUsage(cpuUsage: VelaCpuUsageEntity)

    @Query("SELECT * FROM vela_ram_usage WHERE id = 0")
    fun observeRamUsage(): Flow<VelaRamUsageEntity?>

    @Upsert
    suspend fun upsertRamUsage(ramUsage: VelaRamUsageEntity)

    @Query("SELECT * FROM vela_gpu_usage")
    fun observeGpuUsage(): Flow<List<VelaGpuUsageEntity>>

    @Upsert
    suspend fun upsertGpuUsage(gpuUsage: List<VelaGpuUsageEntity>)

    @Query("DELETE FROM vela_gpu_usage")
    suspend fun clearGpuUsage()

    @Transaction
    suspend fun replaceGpuUsage(gpuUsage: List<VelaGpuUsageEntity>) {
        clearGpuUsage()
        if (gpuUsage.isNotEmpty()) {
            upsertGpuUsage(gpuUsage)
        }
    }

    @Query("SELECT * FROM vela_disk_io")
    fun observeDiskIo(): Flow<List<VelaDiskIoEntity>>

    @Upsert
    suspend fun upsertDiskIo(diskIo: List<VelaDiskIoEntity>)

    @Query("DELETE FROM vela_disk_io")
    suspend fun clearDiskIo()

    @Transaction
    suspend fun replaceDiskIo(diskIo: List<VelaDiskIoEntity>) {
        clearDiskIo()
        if (diskIo.isNotEmpty()) {
            upsertDiskIo(diskIo)
        }
    }

    @Query("SELECT * FROM vela_network_io")
    fun observeNetworkIo(): Flow<List<VelaNetworkIoEntity>>

    @Upsert
    suspend fun upsertNetworkIo(networkIo: List<VelaNetworkIoEntity>)

    @Query("DELETE FROM vela_network_io")
    suspend fun clearNetworkIo()

    @Transaction
    suspend fun replaceNetworkIo(networkIo: List<VelaNetworkIoEntity>) {
        clearNetworkIo()
        if (networkIo.isNotEmpty()) {
            upsertNetworkIo(networkIo)
        }
    }

    @Query("SELECT * FROM vela_temperatures")
    fun observeTemperatures(): Flow<List<VelaTemperatureEntity>>

    @Upsert
    suspend fun upsertTemperatures(temperatures: List<VelaTemperatureEntity>)

    @Query("DELETE FROM vela_temperatures")
    suspend fun clearTemperatures()

    @Transaction
    suspend fun replaceTemperatures(temperatures: List<VelaTemperatureEntity>) {
        clearTemperatures()
        if (temperatures.isNotEmpty()) {
            upsertTemperatures(temperatures)
        }
    }

    @Query("SELECT * FROM vela_sensors")
    fun observeSensors(): Flow<List<VelaSensorEntity>>

    @Upsert
    suspend fun upsertSensors(sensors: List<VelaSensorEntity>)

    @Query("DELETE FROM vela_sensors")
    suspend fun clearSensors()

    @Transaction
    suspend fun replaceSensors(sensors: List<VelaSensorEntity>) {
        clearSensors()
        if (sensors.isNotEmpty()) {
            upsertSensors(sensors)
        }
    }

    @Query("SELECT * FROM vela_fans")
    fun observeFans(): Flow<List<VelaFanEntity>>

    @Upsert
    suspend fun upsertFans(fans: List<VelaFanEntity>)

    @Query("DELETE FROM vela_fans")
    suspend fun clearFans()

    @Transaction
    suspend fun replaceFans(fans: List<VelaFanEntity>) {
        clearFans()
        if (fans.isNotEmpty()) {
            upsertFans(fans)
        }
    }

    @Query("SELECT * FROM vela_battery WHERE id = 0")
    fun observeBattery(): Flow<VelaBatteryEntity?>

    @Upsert
    suspend fun upsertBattery(battery: VelaBatteryEntity)

    @Query("SELECT * FROM vela_clipboard WHERE id = 0")
    fun observeClipboard(): Flow<VelaClipboardEntity?>

    @Upsert
    suspend fun upsertClipboard(clipboard: VelaClipboardEntity)

    @Query("DELETE FROM vela_clipboard")
    suspend fun clearClipboard()

    @Query("SELECT * FROM vela_active_window WHERE id = 0")
    fun observeActiveWindow(): Flow<VelaActiveWindowEntity?>

    @Upsert
    suspend fun upsertActiveWindow(activeWindow: VelaActiveWindowEntity)

    @Query("SELECT * FROM vela_scheduled_tasks")
    fun observeScheduledTasks(): Flow<List<VelaScheduledTaskEntity>>

    @Upsert
    suspend fun upsertScheduledTasks(tasks: List<VelaScheduledTaskEntity>)

    @Query("DELETE FROM vela_scheduled_tasks")
    suspend fun clearScheduledTasks()

    @Query("DELETE FROM vela_scheduled_tasks WHERE id = :taskId")
    suspend fun deleteScheduledTask(taskId: String)

    @Transaction
    suspend fun replaceScheduledTasks(tasks: List<VelaScheduledTaskEntity>) {
        clearScheduledTasks()
        if (tasks.isNotEmpty()) {
            upsertScheduledTasks(tasks)
        }
    }

    @Query("SELECT * FROM vela_files WHERE parentPath = :parentPath")
    fun observeFiles(parentPath: String): Flow<List<VelaFileEntity>>

    @Upsert
    suspend fun upsertFiles(files: List<VelaFileEntity>)

    @Query("DELETE FROM vela_files WHERE parentPath = :parentPath")
    suspend fun clearFiles(parentPath: String)

    @Transaction
    suspend fun replaceFiles(parentPath: String, files: List<VelaFileEntity>) {
        clearFiles(parentPath)
        if (files.isNotEmpty()) {
            upsertFiles(files)
        }
    }

    @Query("SELECT * FROM vela_config WHERE id = 0")
    fun observeConfig(): Flow<VelaConfigEntity?>

    @Upsert
    suspend fun upsertConfig(config: VelaConfigEntity)
}
