package com.template.app.core.sync

import android.util.Log
import com.template.app.domain.repository.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val velaRepository: VelaRepository
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
                    launch { velaRepository.getHealth() },
                    launch { velaRepository.getCpuUsage() },
                    launch { velaRepository.getRamUsage() },
                    launch { velaRepository.getNowPlaying() },
                    launch { velaRepository.getNotifications() },
                    launch { velaRepository.getWifiStatus() },
                    launch { velaRepository.getVolume() },
                    launch { velaRepository.getBrightness() },
                    launch { velaRepository.getNetworkInfo() },
                    launch { velaRepository.getDiskUsage() },
                    launch { velaRepository.getProcesses() },
                    launch { velaRepository.getActiveWindow() },
                    launch { velaRepository.getResolution() },
                    launch { velaRepository.getAudioDevices() },
                    launch { velaRepository.getProcesses() },
                    launch { velaRepository.getBluetoothDevices() },
                    launch { velaRepository.getScheduledTasks() },
                    launch { velaRepository.getPowerProfile() },

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
