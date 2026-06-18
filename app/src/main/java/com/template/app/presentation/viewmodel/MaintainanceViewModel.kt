package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaService
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceUiState(
    val services: List<VelaService> = emptyList(),
    val availableUpdates: List<com.template.app.domain.model.VelaPackageUpdate> = emptyList(),
    val recentLogs: List<String> = emptyList(),
    val logFilter: String = "system",
    val logLinesCount: Int = 50,
    val isLoading: Boolean = false,
    val error: String? = null,
    val testResult: MaintenanceViewModel.MaintenanceTaskResult = MaintenanceViewModel.MaintenanceTaskResult.Idle
)

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: VelaRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    sealed class MaintenanceTaskResult {
        object Idle : MaintenanceTaskResult()
        object Loading : MaintenanceTaskResult()
        object Success : MaintenanceTaskResult()
        data class Error(val message: String) : MaintenanceTaskResult()
    }

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadServices()
            loadUpdates()
            fetchLogs()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // --- Services ---
    private suspend fun loadServices() {
        when (val result = repository.getServices()) {
            is Resource.Success -> {
                _uiState.update { it.copy(services = result.data ?: emptyList()) }
            }
            is Resource.Error -> {
                appEventManager.showActionErrorSnackbar(result.message ?: "Failed to load services")
            }
            else -> Unit
        }
    }

    fun startService(name: String) = performServiceAction(name, "started") { repository.startService(name) }
    fun stopService(name: String) = performServiceAction(name, "stopped") { repository.stopService(name) }
    fun restartService(name: String) = performServiceAction(name, "restarted") { repository.restartService(name) }

    private fun performServiceAction(serviceName: String, actionVerb: String, action: suspend () -> Resource<Unit>) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = action()) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Service $serviceName $actionVerb")
                    loadServices()
                }
                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                }
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    // --- Quick Actions ---
    fun clearCache() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = repository.clearCache()) {
                is Resource.Success -> appEventManager.showActionSuccessSnackbar("Cache cleared")
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    fun syncTime() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = repository.syncTime()) {
                is Resource.Success -> appEventManager.showActionSuccessSnackbar("Time synchronized")
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    // --- Updates ---
    private suspend fun loadUpdates() {
        when (val result = repository.checkUpdates()) {
            is Resource.Success -> {
                _uiState.update { state ->
                    state.copy(availableUpdates = result.data.packages)
                }
            }
            else -> Unit
        }
    }

    fun runUpdates() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = repository.runUpdates()) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Updates completed successfully")
                    _uiState.update { it.copy(availableUpdates = emptyList()) }
                }
                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message ?: "Update process failed")
                }
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    // --- Logs ---
    fun updateLogFilter(filter: String) {
        _uiState.update { it.copy(logFilter = filter) }
        fetchLogs()
    }

    fun updateLogLines(count: Int) {
        _uiState.update { it.copy(logLinesCount = count) }
        fetchLogs()
    }

    fun fetchLogs() {
        viewModelScope.launch {
            val filter = _uiState.value.logFilter
            val lines = _uiState.value.logLinesCount
            when (val result = repository.getLogs(filter, lines)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(recentLogs = result.data?.lines ?: emptyList()) }
                }
                else -> Unit
            }
        }
    }
}
