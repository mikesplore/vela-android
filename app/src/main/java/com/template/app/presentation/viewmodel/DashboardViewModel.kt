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
    val media: VelaMediaState? = null,
    val audio: VelaAudioState? = null,
    val brightness: Int = 0,
    val processes: List<VelaProcess> = emptyList(),
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

    init {
        startPolling()
        startUptimeTicking()
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
            val networkRes = velaRepository.getNetworkInfo()
            val mediaRes = velaRepository.getNowPlaying()
            val audioRes = velaRepository.getVolume()
            val brightnessRes = velaRepository.getBrightness()
            val processesRes = velaRepository.getProcesses()
            val activeWindowRes = velaRepository.getActiveWindow()
            val diskRes = velaRepository.getDiskUsage()
            val clipboardRes = velaRepository.readClipboard()

            _state.update { 
                it.copy(
                    isRefreshing = false,
                    isConnected = healthRes is Resource.Success,
                    health = healthRes.dataOrNull(),
                    network = networkRes.dataOrNull(),
                    media = mediaRes.dataOrNull(),
                    audio = audioRes.dataOrNull(),
                    brightness = brightnessRes.dataOrNull() ?: it.brightness,
                    processes = processesRes.dataOrNull() ?: it.processes,
                    activeWindow = activeWindowRes.dataOrNull(),
                    disks = diskRes.dataOrNull() ?: it.disks,
                    clipboardText = clipboardRes.dataOrNull() ?: it.clipboardText,
                    error = (healthRes as? Resource.Error)?.message,
                    uptimeSeconds = healthRes.dataOrNull()?.uptimeSeconds ?: it.uptimeSeconds
                )
            }
        }
    }

    fun setVolume(value: Int) {
        viewModelScope.launch {
            when (val res = velaRepository.setVolume(value)) {
                is Resource.Success -> _state.update { it.copy(audio = res.data) }
                else -> {}
            }
        }
    }

    fun setMute(muted: Boolean) {
        viewModelScope.launch {
            when (val res = velaRepository.setMute(muted)) {
                is Resource.Success -> _state.update { it.copy(audio = res.data) }
                else -> {}
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            when (velaRepository.setBrightness(value)) {
                is Resource.Success -> _state.update { it.copy(brightness = value) }
                else -> {}
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            velaRepository.togglePlayPause()
            val mediaRes = velaRepository.getNowPlaying()
            _state.update { it.copy(media = mediaRes.dataOrNull()) }
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
        viewModelScope.launch {
            velaRepository.lockDisplay()
        }
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
