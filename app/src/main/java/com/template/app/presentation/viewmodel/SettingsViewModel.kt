package com.template.app.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.push.PushPreferences
import com.template.app.core.push.PushRegistrar
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.HostCapabilities
import com.template.app.domain.model.ModuleKeys
import com.template.app.domain.model.PairedDevice
import com.template.app.domain.model.VelaDevice
import com.template.app.domain.repository.CapabilitiesRepository
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
    val biometricsEnabled: Boolean = false,
    val device: VelaDevice? = null,
    val agentVersion: String = "Unknown",
    val pairedDevices: List<PairedDevice> = emptyList(),
    val renameTargetId: Long? = null,
    val pushRegistered: Boolean = false,
    val pushBusy: Boolean = false,
    val pushAvailable: Boolean = false,
    val firebaseReady: Boolean = false,
    val notificationPermissionGranted: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val switchDeviceUseCase: SwitchDeviceUseCase,
    private val renameDeviceUseCase: RenameDeviceUseCase,
    private val removeDeviceUseCase: RemoveDeviceUseCase,
    private val removeAllDevicesUseCase: RemoveAllDevicesUseCase,
    private val velaRepository: HealthRepository,
    private val capabilitiesRepository: CapabilitiesRepository,
    private val pushRegistrar: PushRegistrar,
    private val pushPreferences: PushPreferences,
    private val appEventManager: AppEventManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _state.update {
                    it.copy(
                        themeMode = settings.themeMode,
                        biometricsEnabled = settings.biometricsEnabled
                    )
                }
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

        viewModelScope.launch {
            capabilitiesRepository.observeCapabilities().collectLatest { caps ->
                refreshPushState(caps)
            }
        }

        refreshDevice()
    }

    private suspend fun refreshPushState(caps: HostCapabilities?) {
        val token = pushPreferences.getLastFcmToken()
        _state.update {
            it.copy(
                pushAvailable = caps?.isModuleAvailable(ModuleKeys.PUSH) == true,
                pushRegistered = !token.isNullOrBlank(),
                firebaseReady = pushRegistrar.isFirebaseAvailable(),
                notificationPermissionGranted = hasNotificationPermission()
            )
        }
    }

    fun refreshDevice() {
        viewModelScope.launch {
            velaRepository.getDevice()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _state.update { it.copy(notificationPermissionGranted = granted) }
        if (granted) setPushEnabled(true)
    }

    fun setPushEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()) {
                    return@launch
                }
                _state.update { it.copy(pushBusy = true) }
                when (val result = pushRegistrar.registerIfPossible()) {
                    is Resource.Success -> {
                        appEventManager.showActionSuccessSnackbar("Push notifications enabled")
                        _state.update { it.copy(pushRegistered = true) }
                    }
                    is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                    else -> {}
                }
                _state.update { it.copy(pushBusy = false) }
            } else {
                _state.update { it.copy(pushBusy = true) }
                when (val result = pushRegistrar.unregisterIfPossible()) {
                    is Resource.Success -> {
                        appEventManager.showActionSuccessSnackbar("Push notifications disabled")
                        _state.update { it.copy(pushRegistered = false) }
                    }
                    is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                    else -> {}
                }
                _state.update { it.copy(pushBusy = false) }
            }
        }
    }

    fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            saveSettingsUseCase.updateTheme(mode)
        }
    }

    fun enableBiometrics(pin: String) {
        viewModelScope.launch {
            saveSettingsUseCase.enableBiometrics(pin)
            appEventManager.showActionSuccessSnackbar("Biometrics enabled")
        }
    }

    fun disableBiometrics() {
        viewModelScope.launch {
            saveSettingsUseCase.disableBiometrics()
            appEventManager.showActionSuccessSnackbar("Biometrics disabled")
        }
    }

    fun updateBiometricPin(pin: String) {
        viewModelScope.launch {
            saveSettingsUseCase.updateBiometricPin(pin)
            appEventManager.showActionSuccessSnackbar("PIN updated")
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
