package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaAudioDevice
import com.template.app.domain.model.VelaAudioState
import com.template.app.domain.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioUiState(
    val audioState: VelaAudioState? = null,
    val devices: List<VelaAudioDevice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        audioRepository.observeAudio()
            .onEach { state -> _uiState.update { it.copy(audioState = state) } }
            .launchIn(viewModelScope)

        audioRepository.observeAudioDevices()
            .onEach { devices -> _uiState.update { it.copy(devices = devices) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            audioRepository.getVolume()
            audioRepository.getAudioDevices()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setVolume(value: Int) {
        viewModelScope.launch {
            val result = audioRepository.setVolume(value)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to set volume")
            }
        }
    }

    fun volumeUp() {
        viewModelScope.launch {
           val result =  audioRepository.volumeUp()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to increase volume")
            }
        }
    }

    fun volumeDown() {
        viewModelScope.launch {
           val result =  audioRepository.volumeDown()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to decrease volume")
            }
        }
    }

    fun toggleMute() {
        val currentState = _uiState.value.audioState ?: return
        viewModelScope.launch {
           val result =  audioRepository.setMute(!currentState.muted)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to ${if (currentState.muted) "unmute" else "mute"} audio")
            }
        }
    }

    fun toggleMicMute() {
        val currentState = _uiState.value.audioState ?: return
        viewModelScope.launch {
            val result = audioRepository.setMicMute(!currentState.micMuted)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to ${if (currentState.micMuted) "unmute" else "mute"} microphone")
            }
        }
    }

    fun selectDevice(device: VelaAudioDevice) {
        viewModelScope.launch {
            val result= audioRepository.setOutputDevice(device.id)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to select device")
            }
            audioRepository.getAudioDevices() // Refresh to update selection state
        }
    }
}
