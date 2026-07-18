package com.template.app.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.usecase.PairDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AddDeviceViewModel @Inject constructor(
    private val pairDeviceUseCase: PairDeviceUseCase,
    private val appEventManager: AppEventManager,
) : ViewModel() {

    private val _baseUrl = MutableStateFlow("")
    val baseUrl = _baseUrl.asStateFlow()

    private val _pairingCode = MutableStateFlow("")
    val pairingCode = _pairingCode.asStateFlow()

    private val _pairingPin = MutableStateFlow("")
    val pairingPin = _pairingPin.asStateFlow()

    private val _showPassword = MutableStateFlow(false)
    val showPassword = _showPassword.asStateFlow()

    private val _testState = MutableStateFlow<OnboardingViewModel.TestResult>(OnboardingViewModel.TestResult.Idle)
    val testState = _testState.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username = _username.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()

    fun setBaseUrl(url: String) {
        _baseUrl.value = url
        _testState.value = OnboardingViewModel.TestResult.Idle
    }

    fun setPairingCode(code: String) {
        _pairingCode.value = code
        _testState.value = OnboardingViewModel.TestResult.Idle
    }

    fun setPairingPin(pin: String) {
        _pairingPin.value = pin
        _testState.value = OnboardingViewModel.TestResult.Idle
    }

    fun toggleShowPassword() {
        _showPassword.value = !_showPassword.value
    }

    fun onQrScanned(scannedValue: String) {
        try {
            val json = JSONObject(scannedValue)
            val vpsUrl = json.optString("vps_url")
            val code = json.optString("pairing_code")
            val pin = json.optString("pairing_pin")
            if (vpsUrl.isNotEmpty() && code.isNotEmpty() && pin.isNotEmpty()) {
                _baseUrl.value = vpsUrl
                _pairingCode.value = code
                _pairingPin.value = pin
                val pairUrl = if (vpsUrl.endsWith("/")) "${vpsUrl}pair/complete" else "$vpsUrl/pair/complete"
                completePairing(pairUrl, code, pin, vpsUrl)
                return
            }
        } catch (_: Exception) {
        }

        try {
            val uri = scannedValue.toUri()
            if (uri.scheme == "vela" && uri.host == "pair") {
                val code = uri.getQueryParameter("code")
                val pin = uri.getQueryParameter("pin")
                if (code != null && pin != null) {
                    completePairing(
                        "https://vela.mikesplore.tech/pair/complete",
                        code,
                        pin,
                        "https://vela.mikesplore.tech"
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    fun manualPairing() {
        val url = _baseUrl.value.trim()
        val code = _pairingCode.value.trim()
        val pin = _pairingPin.value.trim()
        if (url.isEmpty() || code.isEmpty() || pin.isEmpty()) {
            _testState.value = OnboardingViewModel.TestResult.Error("Please fill all pairing fields.")
            return
        }
        val pairUrl = if (url.endsWith("/")) "${url}pair/complete" else "$url/pair/complete"
        completePairing(pairUrl, code, pin, url)
    }

    private fun completePairing(pairUrl: String, code: String, pin: String, vpsUrl: String?) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _testState.value = OnboardingViewModel.TestResult.Testing("Pairing with relay...")
            when (val result = pairDeviceUseCase(pairUrl, code, pin, vpsUrl = vpsUrl)) {
                is Resource.Success -> {
                    val username = result.data.username ?: result.data.device.displayName
                    _username.value = username
                    _testState.value = OnboardingViewModel.TestResult.Success(username)
                }
                is Resource.Error -> {
                    _testState.value = OnboardingViewModel.TestResult.Error(result.message)
                    appEventManager.showActionErrorSnackbar(result.message)
                }
                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }

    fun finish() {
        _finished.value = true
    }
}
