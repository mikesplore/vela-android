package com.template.app.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaResolution
import com.template.app.domain.repository.DisplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DisplayState(
    val screenshot: Bitmap? = null,
    val brightness: Int = 0,
    val resolution: VelaResolution? = null,
    val isNightLightEnabled: Boolean = false,
    val nightLightTemperature: Int = 4500, // Kelvin (1000 - 10000)
    val rotation: String = "normal",
    val isRefreshing: Boolean = false,
    val isScreenshotLoading: Boolean = false,
    val isNightLightChanging: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val repository: DisplayRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    private val _state = MutableStateFlow(DisplayState())
    val state: StateFlow<DisplayState> = _state.asStateFlow()

    init {
        observeData()
        refreshData()
    }

    private fun observeData() {
        repository.observeBrightness()
            .onEach { b ->
                b?.let { _state.update { s -> s.copy(brightness = it.value) } }
            }
            .launchIn(viewModelScope)

        repository.observeResolution()
            .onEach { res ->
                res?.let { r ->
                    _state.update { s ->
                        s.copy(
                            resolution = r,
                            rotation = r.rotation,
                            isNightLightEnabled = r.nightLightEnabled,
                            nightLightTemperature = r.nightLightTemp
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            repository.getBrightness()
            repository.getResolution()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun takeScreenshot() {
        // Check the specific screenshot loading state
        if (_state.value.isScreenshotLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isScreenshotLoading = true) }
            try {
                when (val result = repository.getScreenshot()) {
                    is Resource.Success -> {
                        val base64Str = result.data
                        if (base64Str.isNotBlank()) {
                            val cleanBase64 = if (base64Str.contains(","))
                                base64Str.substringAfter(",") else base64Str
                            try {
                                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                _state.update { it.copy(screenshot = bitmap) }
                            } catch (e: Exception) {
                                appEventManager.showActionErrorSnackbar("Failed to decode screenshot")
                            }
                        }
                    }
                    is Resource.Error -> appEventManager.showActionErrorSnackbar("Failed to take screenshot")
                    else -> {}
                }
            } finally {
                // Always reset the state
                _state.update { it.copy(isScreenshotLoading = false) }
            }
        }
    }

    fun setBrightness(value: Int) {
        viewModelScope.launch {
            val result = repository.setBrightness(value)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to set brightness")
            }
        }
    }

    fun rotate(orientation: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val result = repository.rotateDisplay(orientation)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to rotate display")
            }
            appEventManager.setLoading(false)
        }
    }

    fun setNightLight(enabled: Boolean, temperature: Int? = null) {
        viewModelScope.launch {
            // 1. Enter "Changing" state
            _state.update { it.copy(isNightLightChanging = true) }

            val finalTemp = temperature ?: _state.value.nightLightTemperature
            val result = repository.setNightLight(enabled, finalTemp)

            if (result is Resource.Success) {
                _state.update {
                    it.copy(
                        isNightLightEnabled = enabled,
                        nightLightTemperature = finalTemp,
                        isNightLightChanging = false // Done
                    )
                }
            } else {
                // 2. Handle Error: Revert the UI state
                _state.update { it.copy(isNightLightChanging = false) }
                appEventManager.showActionErrorSnackbar("Failed to update Night Light")
            }
        }
    }

    fun monitorOff() {
        viewModelScope.launch {
            val result = repository.monitorOff()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to disable monitor")
            }
        }
    }

    fun monitorOn() {
        viewModelScope.launch {
            val result = repository.monitorOn()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to enable monitor")
            }
        }
    }

    fun lockScreen() {
        viewModelScope.launch {
            val result = repository.lockDisplay()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to lock screen")
            }
        }
    }
}