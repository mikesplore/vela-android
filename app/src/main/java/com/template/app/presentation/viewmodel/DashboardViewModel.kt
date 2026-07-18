package com.template.app.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import com.template.app.domain.repository.AudioRepository
import com.template.app.domain.repository.DisplayRepository
import com.template.app.domain.repository.FilesystemRepository
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.repository.MediaRepository
import com.template.app.domain.repository.MonitorRepository
import com.template.app.domain.repository.NetworkRepository
import com.template.app.domain.repository.ProcessesRepository
import com.template.app.domain.usecase.ObserveActiveDeviceUseCase
import com.template.app.domain.usecase.ObserveDevicesUseCase
import com.template.app.domain.usecase.SwitchDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val isConnected: Boolean = false,
    val isRefreshing: Boolean = false,
    val health: VelaHealth? = null,
    val uptime: VelaUptime? = null,
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
    val isScreenshotLoading: Boolean = false,
    val screenshot: Bitmap? = null,
    val activeDevice: PairedDevice? = null,
    val pairedDevices: List<PairedDevice> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val processRepository: ProcessesRepository,
    private val monitorRepository: MonitorRepository,
    private val mediaRepository: MediaRepository,
    private val displayRepository: DisplayRepository,
    private val audioRepository: AudioRepository,
    private val fileRepository: FilesystemRepository,
    private val healthRepository: HealthRepository,
    private val networkRepository: NetworkRepository,
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val observeActiveDeviceUseCase: ObserveActiveDeviceUseCase,
    private val switchDeviceUseCase: SwitchDeviceUseCase,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _processLimit = MutableStateFlow(5)

    init {
        observeData()
    }

    fun setFabVisible(visible: Boolean) {
        appEventManager.setDashboardFabVisible(visible)
    }

    fun switchDevice(id: Long) {
        viewModelScope.launch {
            runCatching { switchDeviceUseCase(id) }
                .onSuccess { appEventManager.showActionSuccessSnackbar("Switched device") }
                .onFailure { appEventManager.showActionErrorSnackbar(it.message ?: "Switch failed") }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        observeActiveDeviceUseCase()
            .onEach { device -> _state.update { it.copy(activeDevice = device) } }
            .launchIn(viewModelScope)

        observeDevicesUseCase()
            .onEach { devices -> _state.update { it.copy(pairedDevices = devices) } }
            .launchIn(viewModelScope)

        healthRepository.observeHealth()
            .onEach { health -> 
                _state.update { it.copy(
                    health = health, 
                    isConnected = health != null
                ) } 
            }
            .launchIn(viewModelScope)

        monitorRepository.observeUptime()
            .onEach { uptime -> _state.update { it.copy(uptime = uptime) } }
            .launchIn(viewModelScope)

        networkRepository.observeNetwork()
            .onEach { network -> _state.update { it.copy(network = network) } }
            .launchIn(viewModelScope)

        networkRepository.observeWifi()
            .onEach { wifi -> _state.update { it.copy(wifi = wifi) } }
            .launchIn(viewModelScope)

        audioRepository.observeAudio()
            .onEach { audio -> _state.update { it.copy(audio = audio) } }
            .launchIn(viewModelScope)

        mediaRepository.observeMedia()
            .onEach { media -> _state.update { it.copy(media = media) } }
            .launchIn(viewModelScope)

        processRepository.observeActiveWindow()
            .onEach { window -> _state.update { it.copy(activeWindow = window) } }
            .launchIn(viewModelScope)

        fileRepository.observeDisks()
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

        displayRepository.observeBrightness()
            .onEach { b -> b?.let { brightness -> _state.update { it.copy(brightness = brightness.value) } } }
            .launchIn(viewModelScope)

        monitorRepository.observeCpuUsage()
            .onEach { cpu -> cpu?.let { usage -> _state.update { it.copy(cpuUsage = usage.overall) } } }
            .launchIn(viewModelScope)

        monitorRepository.observeRamUsage()
            .onEach { ram -> ram?.let { usage -> _state.update { it.copy(ramUsage = usage.percent) } } }
            .launchIn(viewModelScope)

        _processLimit
            .flatMapLatest { limit -> processRepository.observeProcesses(limit) }
            .onEach { processes -> _state.update { it.copy(processes = processes) } }
            .launchIn(viewModelScope)
    }

    fun toggleProcessLimit() {
        val newLimit = if (_processLimit.value == 5) 50 else 5
        _processLimit.value = newLimit
        _state.update { it.copy(processLimit = newLimit) }
    }

    fun setVolume(value: Int) {
        viewModelScope.launch {
            val result = audioRepository.setVolume(value)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to set volume")
            }
        }
    }

    fun setMute(muted: Boolean) {
        viewModelScope.launch {
            val result = audioRepository.setMute(muted)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to set mute")
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            val result = displayRepository.setBrightness(value)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to set brightness")
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            val result = mediaRepository.togglePlayPause()
            mediaRepository.getNowPlaying()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to toggle play/pause")
            }
        }
    }

    fun lockScreen() {
        viewModelScope.launch {
            val result = displayRepository.lockDisplay()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to lock screen")
            }
        }
    }

    fun takeScreenshot() {
        _state.update { it.copy(isScreenshotLoading = true, screenshot = null) }
        viewModelScope.launch {
            try {
                when (val res = displayRepository.getScreenshot()) {
                    is Resource.Success -> {
                        val base64Str = res.data
                        if (base64Str.isNotBlank()) {
                            val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            _state.update { it.copy(screenshot = bitmap, isScreenshotLoading = false) }
                        } else {
                            _state.update { it.copy(isScreenshotLoading = false) }
                        }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(isScreenshotLoading = false) }
                        appEventManager.showActionErrorSnackbar("Failed to take screenshot")
                    }
                    else -> {
                        _state.update { it.copy(isScreenshotLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isScreenshotLoading = false) }
            }
        }
    }

    fun dismissScreenshot() {
        _state.update { it.copy(screenshot = null, isScreenshotLoading = false) }
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
