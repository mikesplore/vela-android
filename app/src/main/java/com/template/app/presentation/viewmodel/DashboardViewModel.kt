package com.template.app.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.sync.DataSyncManager
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import com.template.app.domain.repository.VelaRepository
import com.template.app.domain.usecase.ClearSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val isConnected: Boolean = false,
    val isRefreshing: Boolean = false,
    val health: VelaHealth? = null,
    val network: VelaNetworkInfo? = null,
    val wifi: VelaWifiStatus? = null,
    val resolution: VelaResolution? = null,
    val media: VelaMediaState? = null,
    val audio: VelaAudioState? = null,
    val brightness: Int = 0,
    val processes: List<VelaProcess> = emptyList(),
    val processLimit: Int = 5,
    val cpuUsage: Double = 0.0,
    val ramUsage: Double = 0.0,
    val activeWindow: String? = null,
    val disks: List<VelaDiskUsage> = emptyList(),
    val clipboardText: String = "",
    val error: String? = null,
    val uptimeSeconds: Long = 0,
    val screenshot: Bitmap? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val velaRepository: VelaRepository,
    private val clearSettingsUseCase: ClearSettingsUseCase,
    private val dataSyncManager: DataSyncManager,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _processLimit = MutableStateFlow(5)

    private val _isFabExpanded = MutableStateFlow(false)
    val isFabExpanded: StateFlow<Boolean> = _isFabExpanded.asStateFlow()


    init {
        observeData()
        startUptimeTicking()
    }

    fun setFabVisible(visible: Boolean) {
        appEventManager.setDashboardFabVisible(visible)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        // Observe Sync Status
        combine(
            dataSyncManager.isSyncing,
            velaRepository.observeHealth()
        ) { syncing, health ->
            // Show overlay ONLY if syncing AND we don't have health data yet (initial load)
            val shouldShowLoading = syncing && health == null
            appEventManager.setLoading(shouldShowLoading)
        }.launchIn(viewModelScope)

        // Observe all data streams from Room DB
        velaRepository.observeHealth()
            .onEach { health -> 
                _state.update { it.copy(
                    health = health, 
                    uptimeSeconds = health?.uptimeSeconds ?: it.uptimeSeconds,
                    isConnected = health != null
                ) } 
            }
            .launchIn(viewModelScope)

        velaRepository.observeNetwork()
            .onEach { network -> _state.update { it.copy(network = network) } }
            .launchIn(viewModelScope)

        velaRepository.observeWifi()
            .onEach { wifi -> _state.update { it.copy(wifi = wifi) } }
            .launchIn(viewModelScope)

        velaRepository.observeAudio()
            .onEach { audio -> _state.update { it.copy(audio = audio) } }
            .launchIn(viewModelScope)

        velaRepository.observeMedia()
            .onEach { media -> _state.update { it.copy(media = media) } }
            .launchIn(viewModelScope)

        velaRepository.observeActiveWindow()
            .onEach { window -> _state.update { it.copy(activeWindow = window) } }
            .launchIn(viewModelScope)

        velaRepository.observeDisks()
            .onEach { rawDisks ->
                val formattedDisks = rawDisks.map { disk ->
                    disk.copy(
                        used = formatBytes(disk.used),
                        total = formatBytes(disk.total)
                    )
                }
                _state.update { it.copy(disks = formattedDisks) }
            }
            .launchIn(viewModelScope)

        velaRepository.observeBrightness()
            .onEach { b -> b?.let { brightness -> _state.update { it.copy(brightness = brightness.value) } } }
            .launchIn(viewModelScope)

        velaRepository.observeCpuUsage()
            .onEach { cpu -> cpu?.let { usage -> _state.update { it.copy(cpuUsage = usage.overall) } } }
            .launchIn(viewModelScope)

        velaRepository.observeRamUsage()
            .onEach { ram -> ram?.let { usage -> _state.update { it.copy(ramUsage = usage.percent) } } }
            .launchIn(viewModelScope)

        velaRepository.observeClipboard()
            .onEach { clip -> _state.update { it.copy(clipboardText = clip?.content ?: "") } }
            .launchIn(viewModelScope)

        _processLimit
            .flatMapLatest { limit -> velaRepository.observeProcesses(limit) }
            .onEach { processes -> _state.update { it.copy(processes = processes) } }
            .launchIn(viewModelScope)
    }

    private fun startUptimeTicking() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_state.value.isConnected) {
                    _state.update { it.copy(uptimeSeconds = it.uptimeSeconds + 1) }
                }
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            dataSyncManager.performSyncCycle()
        }
    }

    fun toggleProcessLimit() {
        val newLimit = if (_processLimit.value == 5) 50 else 5
        _processLimit.value = newLimit
        _state.update { it.copy(processLimit = newLimit) }
    }

    fun setVolume(value: Int) {
        viewModelScope.launch { velaRepository.setVolume(value) }
    }

    fun setMute(muted: Boolean) {
        viewModelScope.launch { velaRepository.setMute(muted) }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch { velaRepository.setBrightness(value) }
    }

    fun togglePlayPause() {
        viewModelScope.launch { velaRepository.togglePlayPause() }
    }

    fun writeClipboard(text: String) {
        viewModelScope.launch { velaRepository.writeClipboard(text) }
    }

    fun lockScreen() {
        viewModelScope.launch { velaRepository.lockDisplay() }
    }

    fun takeScreenshot() {
        viewModelScope.launch {
            when (val res = velaRepository.getScreenshot()) {
                is Resource.Success -> {
                    val base64Str = res.data
                    if (base64Str.isNotBlank()) {
                        val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                        val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        _state.update { it.copy(screenshot = bitmap) }
                    }
                }
                else -> {}
            }
        }
    }
    
    fun dismissScreenshot() {
        _state.update { it.copy(screenshot = null) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            clearSettingsUseCase()
            onComplete()
        }
    }

    private fun formatBytes(bytesStr: String?): String {
        if (bytesStr.isNullOrBlank() || bytesStr == "0") return "0.0 B"
        val cleanInput = bytesStr.split("\n").firstOrNull()?.trim() ?: "0"
        val bytes = cleanInput.toLongOrNull() ?: return bytesStr
        if (bytes < 1024) return "$bytes B"
        val exp = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
        val units = arrayOf("KB", "MB", "GB", "TB", "PB")
        return String.format(Locale.ROOT, "%.1f %s", bytes.toDouble() / (1L shl (exp * 10)), units[exp - 1])
    }
}
