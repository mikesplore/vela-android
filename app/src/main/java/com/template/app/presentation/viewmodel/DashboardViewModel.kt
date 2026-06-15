package com.template.app.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import com.template.app.domain.repository.VelaRepository
import com.template.app.domain.usecase.ClearSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val clearSettingsUseCase: ClearSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _processLimit = MutableStateFlow(5)

    init {
        observeData()
        startPolling()
        startUptimeTicking()
    }

    private fun observeData() {
        velaRepository.observeHealth()
            .onEach { health -> _state.update { it.copy(health = health, uptimeSeconds = health?.uptimeSeconds ?: it.uptimeSeconds) } }
            .launchIn(viewModelScope)

        velaRepository.observeNetwork()
            .onEach { network -> _state.update { it.copy(network = network) } }
            .launchIn(viewModelScope)

        velaRepository.observeWifi()
            .onEach { wifi -> _state.update { it.copy(wifi = wifi) } }
            .launchIn(viewModelScope)

        velaRepository.observeResolution()
            .onEach { res -> _state.update { it.copy(resolution = res) } }
            .launchIn(viewModelScope)

        velaRepository.observeAudio()
            .onEach { audio -> _state.update { it.copy(audio = audio) } }
            .launchIn(viewModelScope)

        velaRepository.observeMedia()
            .onEach { media -> _state.update { it.copy(media = media) } }
            .launchIn(viewModelScope)

        velaRepository.observeDisks()
            .onEach { disks -> _state.update { it.copy(disks = disks) } }
            .launchIn(viewModelScope)

        velaRepository.observeBrightness()
            .onEach { b -> b?.let { brightness -> _state.update { it.copy(brightness = brightness.value) } } }
            .launchIn(viewModelScope)

        _processLimit
            .flatMapLatest { limit -> velaRepository.observeProcesses(limit) }
            .onEach { processes -> _state.update { it.copy(processes = processes) } }
            .launchIn(viewModelScope)
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                refreshAllData()
                delay(5000)
            }
        }
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
            _state.update { it.copy(isRefreshing = true) }
            
            val healthRes = velaRepository.getHealth()
            velaRepository.getNetworkInfo()
            velaRepository.getWifiStatus()
            velaRepository.getResolution()
            velaRepository.getNowPlaying()
            velaRepository.getVolume()
            velaRepository.getBrightness()
            velaRepository.getProcesses()
            val activeWindowRes = velaRepository.getActiveWindow()
            velaRepository.getDiskUsage()
            val clipboardRes = velaRepository.readClipboard()

            _state.update { 
                it.copy(
                    isRefreshing = false,
                    isConnected = healthRes is Resource.Success,
                    activeWindow = activeWindowRes.dataOrNull(),
                    clipboardText = clipboardRes.dataOrNull() ?: it.clipboardText,
                    error = (healthRes as? Resource.Error)?.message
                )
            }
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
        viewModelScope.launch {
            velaRepository.togglePlayPause()
            velaRepository.getNowPlaying()
        }
    }

    fun writeClipboard(text: String) {
        viewModelScope.launch {
            when (velaRepository.writeClipboard(text)) {
                is Resource.Success -> _state.update { it.copy(clipboardText = text) }
                else -> {}
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            clearSettingsUseCase()
            onComplete()
        }
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

    private fun <T> Resource<T>.dataOrNull(): T? = (this as? Resource.Success)?.data
}
