package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.data.local.LegacyConnectionRestorer
import com.template.app.core.sync.DataSyncManager
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.VelaHealth
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.HasDevicesUseCase
import com.template.app.domain.usecase.ObserveActiveDeviceUseCase
import com.template.app.presentation.ui.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val hasDevicesUseCase: HasDevicesUseCase,
    private val observeActiveDeviceUseCase: ObserveActiveDeviceUseCase,
    private val legacyConnectionRestorer: LegacyConnectionRestorer,
    private val dataSyncManager: DataSyncManager,
    private val healthRepository: HealthRepository,
    val appEventManager: AppEventManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    val health: StateFlow<VelaHealth?> = healthRepository.observeHealth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            legacyConnectionRestorer.restoreIfNeeded()

            val hasDevices = hasDevicesUseCase()
            if (_startDestination.value == null) {
                _startDestination.value = if (hasDevices) Routes.MAIN else Routes.ONBOARDING
            }

            if (hasDevices) {
                dataSyncManager.startSync()
            }

            launch {
                getSettingsUseCase().collectLatest { settings ->
                    _themeMode.value = settings.themeMode
                }
            }

            launch {
                observeActiveDeviceUseCase().collectLatest { device ->
                    if (device != null) {
                        dataSyncManager.startSync()
                    } else {
                        dataSyncManager.stopSync()
                        if (_startDestination.value == Routes.MAIN) {
                            // All devices removed while on main — keep destination;
                            // navigation is handled by Settings/remove callbacks.
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataSyncManager.stopSync()
    }
}
