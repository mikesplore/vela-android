package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.PairedDevice
import com.template.app.domain.model.VelaDevice
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.ObserveDevicesUseCase
import com.template.app.domain.usecase.RemoveAllDevicesUseCase
import com.template.app.domain.usecase.RemoveDeviceUseCase
import com.template.app.domain.usecase.RenameDeviceUseCase
import com.template.app.domain.usecase.SaveSettingsUseCase
import com.template.app.domain.usecase.SwitchDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val device: VelaDevice? = null,
    val agentVersion: String = "Unknown",
    val pairedDevices: List<PairedDevice> = emptyList(),
    val renameTargetId: Long? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val switchDeviceUseCase: SwitchDeviceUseCase,
    private val renameDeviceUseCase: RenameDeviceUseCase,
    private val removeDeviceUseCase: RemoveDeviceUseCase,
    private val removeAllDevicesUseCase: RemoveAllDevicesUseCase,
    private val velaRepository: HealthRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _state.update { it.copy(themeMode = settings.themeMode) }
            }
        }

        viewModelScope.launch {
            velaRepository.observeDevice().collectLatest { device ->
                _state.update { it.copy(device = device) }
            }
        }

        viewModelScope.launch {
            observeDevicesUseCase().collectLatest { devices ->
                _state.update { it.copy(pairedDevices = devices) }
            }
        }

        refreshDevice()
    }

    fun refreshDevice() {
        viewModelScope.launch {
            velaRepository.getDevice()
        }
    }

    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase.updateTheme(mode)
        }
    }

    fun switchDevice(id: Long) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            runCatching { switchDeviceUseCase(id) }
                .onSuccess { appEventManager.showActionSuccessSnackbar("Switched device") }
                .onFailure { appEventManager.showActionErrorSnackbar(it.message ?: "Switch failed") }
            appEventManager.setLoading(false)
        }
    }

    fun renameDevice(id: Long, label: String) {
        viewModelScope.launch {
            renameDeviceUseCase(id, label)
            _state.update { it.copy(renameTargetId = null) }
        }
    }

    fun setRenameTarget(id: Long?) {
        _state.update { it.copy(renameTargetId = id) }
    }

    fun removeDevice(id: Long, onNoneRemain: () -> Unit) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val remaining = removeDeviceUseCase(id)
            appEventManager.showActionSuccessSnackbar("Device removed")
            if (!remaining) onNoneRemain()
            appEventManager.setLoading(false)
        }
    }

    fun removeAllDevices(onComplete: () -> Unit) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            removeAllDevicesUseCase()
            appEventManager.showActionSuccessSnackbar("All devices removed")
            onComplete()
            appEventManager.setLoading(false)
        }
    }
}
