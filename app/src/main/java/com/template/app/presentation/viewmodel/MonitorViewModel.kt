package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.domain.model.*
import com.template.app.domain.repository.MonitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
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

        repository.observeTopProcessesByCpu(20)
            .onEach { data -> _state.update { it.copy(topProcessesByCpu = data) } }
            .launchIn(viewModelScope)

        repository.observeTopProcessesByMemory(20)
            .onEach { data -> _state.update { it.copy(topProcessesByMemory = data) } }
            .launchIn(viewModelScope)
    }

    /** Direct endpoint polls — not via snapshot. */
    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(isRefreshing = true) }
                repository.getCpuUsage()
                repository.getRamUsage()
                repository.getDiskIo()
                repository.getNetworkIo()
                repository.getMonitorProcesses()
                _state.update { it.copy(isRefreshing = false) }
                delay(5_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            repository.getDiskIo()
            repository.getNetworkIo()
            repository.getMonitorProcesses()
            repository.getCpuUsage()
            repository.getRamUsage()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
