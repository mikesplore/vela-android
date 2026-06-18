package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val velaRepository: VelaRepository,
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

    fun toggleWifi(enabled: Boolean) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isWifiToggling = true) }
            velaRepository.toggleWifi(enabled)
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
                    appEventManager.showActionSuccessSnackbar("Ping completed")
                }

                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {}
            }
            _state.update { it.copy(isPinging = false) }
        }
    }

    fun runSpeedTest() {
        viewModelScope.launch {
            appEventManager.showActionSuccessSnackbar("Starting speed test...")
            _state.update { it.copy(isSpeedTesting = true, speedTest = null) }
            when (val result = velaRepository.runSpeedTest()) {
                is Resource.Success -> {
                    _state.update { it.copy(speedTest = result.data) }
                    appEventManager.showActionSuccessSnackbar("Speed test finished")
                }

                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                    appEventManager.showActionErrorSnackbar(result.message)
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
            if (device.isPaired) {
                velaRepository.unpairBluetooth(device.id)
            } else {
                velaRepository.pairBluetooth(device.id)
            }
            fetchBluetoothDevices()
        }
    }
}
