package com.template.app.core.sync

import android.util.Log
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.repository.AudioRepository
import com.template.app.domain.repository.DisplayRepository
import com.template.app.domain.repository.FilesystemRepository
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.repository.MediaRepository
import com.template.app.domain.repository.MonitorRepository
import com.template.app.domain.repository.NetworkRepository
import com.template.app.domain.repository.PowerRepository
import com.template.app.domain.repository.ProcessesRepository
import com.template.app.domain.repository.SchedulesRepository
import com.template.app.domain.repository.UserRepository
import com.template.app.domain.usecase.ClearSettingsUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncManager @Inject constructor(
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
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun startSync() {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch {
            while (isActive) {
                performSyncCycle()
                delay(10_000) // 10 seconds
            }
        }
    }

    suspend fun performSyncCycle() {
        if (_isSyncing.value) return
        
        try {
            _isSyncing.value = true
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
                    launch { processRepository.getProcesses() },
                    launch { processRepository.getActiveWindow() },
                    launch { displayRepository.getResolution() },
                    launch { audioRepository.getAudioDevices() },
                    launch { processRepository.getProcesses() },
                    launch { networkRepository.getBluetoothDevices() },
                    launch { schedulerRepository.getScheduledTasks() },
                    launch { powerRepository.getPowerProfile() },
                    launch { }

                )
                tasks.joinAll()
            }
            Log.d("DataSyncManager", "Sync cycle completed successfully.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("DataSyncManager", "Sync cycle failed", e)
        } finally {
            _isSyncing.value = false
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        _isSyncing.value = false
    }
}
