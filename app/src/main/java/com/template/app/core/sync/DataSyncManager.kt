package com.template.app.core.sync

import android.util.Log
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.domain.repository.AudioRepository
import com.template.app.domain.repository.ClipboardRepository
import com.template.app.domain.repository.DisplayRepository
import com.template.app.domain.repository.FilesystemRepository
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.repository.MaintenanceRepository
import com.template.app.domain.repository.MediaRepository
import com.template.app.domain.repository.MonitorRepository
import com.template.app.domain.repository.NetworkRepository
import com.template.app.domain.repository.PowerRepository
import com.template.app.domain.repository.ProcessesRepository
import com.template.app.domain.repository.SchedulesRepository
import com.template.app.domain.repository.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncManager @Inject constructor(
    private val activeConnection: ActiveConnectionProvider,
    private val userRepository: UserRepository,
    private val processRepository: ProcessesRepository,
    private val monitorRepository: MonitorRepository,
    private val mediaRepository: MediaRepository,
    private val displayRepository: DisplayRepository,
    private val audioRepository: AudioRepository,
    private val fileRepository: FilesystemRepository,
    private val healthRepository: HealthRepository,
    private val networkRepository: NetworkRepository,
    private val schedulerRepository: SchedulesRepository,
    private val powerRepository: PowerRepository,
    private val clipboardRepository: ClipboardRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null
    private var watchJob: Job? = null

    fun startSync() {
        if (watchJob?.isActive == true) return

        watchJob = scope.launch {
            activeConnection.connectionId
                .collectLatest { connectionId ->
                    healthRepository.clearInMemoryCaches()
                    stopSyncLoop()
                    if (connectionId != null) {
                        startSyncLoop()
                    }
                }
        }
    }

    private fun startSyncLoop() {
        if (syncJob?.isActive == true) return
        if (activeConnection.current() == null) return
        syncJob = scope.launch {
            while (isActive) {
                performSyncCycle()
                delay(10_000)
            }
        }
    }

    private fun stopSyncLoop() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun performSyncCycle() {
        if (activeConnection.current() == null) return

        try {
            Log.d("DataSyncManager", "Starting data sync cycle...")

            coroutineScope {
                val tasks = listOf(
                    launch { userRepository.fetchUsers() },
                    launch { healthRepository.getHealth() },
                    launch { monitorRepository.getCpuUsage() },
                    launch { monitorRepository.getRamUsage() },
                    launch { mediaRepository.getNowPlaying() },
                    launch { networkRepository.getWifiStatus() },
                    launch { audioRepository.getVolume() },
                    launch { displayRepository.getBrightness() },
                    launch { networkRepository.getNetworkInfo() },
                    launch { fileRepository.getDiskUsage() },
                    launch { processRepository.getActiveWindow() },
                    launch { displayRepository.getResolution() },
                    launch { audioRepository.getAudioDevices() },
                    launch { networkRepository.getBluetoothDevices() },
                    launch { schedulerRepository.getScheduledTasks() },
                    launch { powerRepository.getPowerProfile() },
                    launch { clipboardRepository.readClipboard() },
                    launch { monitorRepository.getUptime() },
                    launch { monitorRepository.getDiskIo() },
                    launch { monitorRepository.getNetworkIo() },
                    launch { monitorRepository.getMonitorProcesses() },
                    launch { maintenanceRepository.getServices() }
                )
                tasks.joinAll()
            }
            Log.d("DataSyncManager", "Sync cycle completed successfully.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("DataSyncManager", "Sync cycle failed", e)
        }
    }

    fun stopSync() {
        watchJob?.cancel()
        watchJob = null
        stopSyncLoop()
    }

    fun restartSync() {
        stopSync()
        healthRepository.clearInMemoryCaches()
        startSync()
    }
}
