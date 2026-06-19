package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import com.template.app.domain.repository.MonitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonitorState(
    val cpu: VelaCpuUsage? = null,
    val ram: VelaRamUsage? = null,
    val gpu: List<VelaGpuUsage> = emptyList(),
    val diskIo: List<VelaDiskIo> = emptyList(),
    val networkIo: List<VelaNetworkIo> = emptyList(),
    val temperatures: List<VelaTemperatureInfo> = emptyList(),
    val fans: List<VelaFanInfo> = emptyList(),
    val sensors: List<VelaSensorInfo> = emptyList(),
    val battery: VelaBatteryStatus? = null,
    val topProcessesByCpu: List<VelaProcess> = emptyList(),
    val topProcessesByMemory: List<VelaProcess> = emptyList(),
    val updateIntervalMs: Long = 2000,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val repository: MonitorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MonitorState())
    val state = _state.asStateFlow()

    private var pollingJob: Job? = null

    init {
        observeMonitorData()
        startPolling()
    }

    private fun observeMonitorData() {
        repository.observeCpuUsage()
            .onEach { data -> _state.update { it.copy(cpu = data) } }
            .launchIn(viewModelScope)

        repository.observeRamUsage()
            .onEach { data -> _state.update { it.copy(ram = data) } }
            .launchIn(viewModelScope)

        repository.observeGpuUsage()
            .onEach { data -> _state.update { it.copy(gpu = data) } }
            .launchIn(viewModelScope)

        repository.observeDiskIo()
            .onEach { data -> _state.update { it.copy(diskIo = data) } }
            .launchIn(viewModelScope)

        repository.observeNetworkIo()
            .onEach { data -> _state.update { it.copy(networkIo = data) } }
            .launchIn(viewModelScope)

        repository.observeTemperatures()
            .onEach { data -> _state.update { it.copy(temperatures = data) } }
            .launchIn(viewModelScope)

        repository.observeFans()
            .onEach { data -> _state.update { it.copy(fans = data) } }
            .launchIn(viewModelScope)

        repository.observeSensors()
            .onEach { data -> _state.update { it.copy(sensors = data) } }
            .launchIn(viewModelScope)

        repository.observeBattery()
            .onEach { data -> _state.update { it.copy(battery = data) } }
            .launchIn(viewModelScope)

        repository.observeTopProcessesByCpu(10)
            .onEach { data -> _state.update { it.copy(topProcessesByCpu = data) } }
            .launchIn(viewModelScope)

        repository.observeTopProcessesByMemory(10)
            .onEach { data -> _state.update { it.copy(topProcessesByMemory = data) } }
            .launchIn(viewModelScope)
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshMonitorData()
                delay(state.value.updateIntervalMs)
            }
        }
    }

    fun setUpdateInterval(intervalMs: Long) {
        _state.update { it.copy(updateIntervalMs = intervalMs) }
        startPolling()
    }

    // MonitorViewModel.kt

    suspend fun refreshMonitorData() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        // We only care about the result for error handling/loading state
        val result = repository.getMonitorSnapshot()
        if (result is Resource.Error) {
            _state.update { it.copy(error = result.message) }
        }
        _state.update { it.copy(isRefreshing = false) }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
