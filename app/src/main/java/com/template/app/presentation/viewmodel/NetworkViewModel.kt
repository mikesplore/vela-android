package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaBluetoothDevice
import com.template.app.domain.model.VelaNetworkInfo
import com.template.app.domain.model.VelaPingResult
import com.template.app.domain.model.VelaSpeedTest
import com.template.app.domain.model.VelaWifiStatus
import com.template.app.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkState(
    val networkInfo: VelaNetworkInfo? = null,
    val wifiStatus: VelaWifiStatus? = null,
    val bluetoothDevices: List<VelaBluetoothDevice> = emptyList(),
    val pingResult: VelaPingResult? = null,
    val speedTest: VelaSpeedTest? = null,
    val isWifiToggling: Boolean = false,
    val isPinging: Boolean = false,
    val isSpeedTesting: Boolean = false,
    val isBluetoothLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val velaRepository: NetworkRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkState())
    val state = _state.asStateFlow()

    init {
        refresh()
        observeData()
    }

    private fun observeData() {
        velaRepository.observeNetwork()
            .onEach { info -> _state.update { it.copy(networkInfo = info) } }
            .launchIn(viewModelScope)

        velaRepository.observeWifi()
            .onEach { status -> _state.update { it.copy(wifiStatus = status) } }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            velaRepository.getNetworkLocation()
            velaRepository.getWifiStatus()
            fetchBluetoothDevices()
        }
    }

    fun disconnectWifi() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val result = velaRepository.disconnectWifi()
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to disconnect")
            }
            velaRepository.getWifiStatus()
            appEventManager.setLoading(false)
        }
    }

    fun toggleWifi(enabled: Boolean) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isWifiToggling = true) }
            val result = velaRepository.toggleWifi(enabled)
            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to ${if (enabled) "enable" else "disable"} wifi")
            }
            velaRepository.getWifiStatus()
            _state.update { it.copy(isWifiToggling = false) }
            appEventManager.setLoading(false)
        }
    }

    fun pingHost(host: String, count: Int = 4) {
        viewModelScope.launch {
            _state.update { it.copy(isPinging = true, pingResult = null) }
            when (val result = velaRepository.pingHost(host, count)) {
                is Resource.Success -> {
                    _state.update { it.copy(pingResult = result.data) }
                }

                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar("Failed to ping")
                }

                else -> {}
            }
            _state.update { it.copy(isPinging = false) }
        }
    }

    fun runSpeedTest() {
        viewModelScope.launch {
            _state.update { it.copy(isSpeedTesting = true, speedTest = null) }
            when (val result = velaRepository.runSpeedTest()) {
                is Resource.Success -> {
                    _state.update { it.copy(speedTest = result.data) }
                }

                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar("Failed to run speed test")
                }

                else -> {}
            }
            _state.update { it.copy(isSpeedTesting = false) }
        }
    }

    fun fetchBluetoothDevices() {
        viewModelScope.launch {
            _state.update { it.copy(isBluetoothLoading = true) }
            when (val result = velaRepository.getBluetoothDevices()) {
                is Resource.Success -> _state.update { it.copy(bluetoothDevices = result.data) }
                else -> {}
            }
            _state.update { it.copy(isBluetoothLoading = false) }
        }
    }

    fun toggleBluetoothPairing(device: VelaBluetoothDevice) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val result = if (device.isPaired) {
                velaRepository.unpairBluetooth(device.id)
            } else {
                velaRepository.pairBluetooth(device.id)
            }

            if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar("Failed to ${if (device.isPaired) "unpair" else "pair"}")
            }

            fetchBluetoothDevices()
            appEventManager.setLoading(false)
        }
    }
}