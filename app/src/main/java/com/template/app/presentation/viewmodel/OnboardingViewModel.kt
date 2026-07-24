package com.template.app.presentation.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.push.PushRegistrar
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.CapabilitiesRepository
import com.template.app.domain.usecase.PairDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val pairDeviceUseCase: PairDeviceUseCase,
    private val capabilitiesRepository: CapabilitiesRepository,
    private val pushRegistrar: PushRegistrar,
    private val appEventManager: AppEventManager,
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _baseUrl = MutableStateFlow("")
    val baseUrl = _baseUrl.asStateFlow()

    private val _pairingCode = MutableStateFlow("")
    val pairingCode = _pairingCode.asStateFlow()

    private val _pairingPin = MutableStateFlow("")
    val pairingPin = _pairingPin.asStateFlow()

    private val _showPassword = MutableStateFlow(false)
    val showPassword = _showPassword.asStateFlow()

    private val _testState = MutableStateFlow<TestResult>(TestResult.Idle)
    val testState = _testState.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username = _username.asStateFlow()

    private val _pairingComplete = MutableStateFlow(false)
    val pairingComplete = _pairingComplete.asStateFlow()

    private val _capabilitiesState =
        MutableStateFlow<CapabilitiesLoadState>(CapabilitiesLoadState.Idle)
    val capabilitiesState = _capabilitiesState.asStateFlow()

    private val _availableModuleCount = MutableStateFlow(0)
    val availableModuleCount = _availableModuleCount.asStateFlow()

    sealed interface TestResult {
        object Idle : TestResult
        data class Testing(val message: String) : TestResult
        data class Success(val username: String) : TestResult
        data class Error(val message: String) : TestResult
    }

    sealed interface CapabilitiesLoadState {
        object Idle : CapabilitiesLoadState
        object Loading : CapabilitiesLoadState
        data class Success(val moduleCount: Int) : CapabilitiesLoadState
        data class Error(val message: String) : CapabilitiesLoadState
    }

    fun nextPage() {
        if (_currentPage.value < 4) {
            val next = _currentPage.value + 1
            _currentPage.value = next
            if (next < 3) {
                resetPairingInputs()
            }
            if (next == 3) {
                loadCapabilities()
            }
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            if (_currentPage.value < 3) {
                resetPairingInputs()
            }
        }
    }

    private fun resetPairingInputs() {
        _testState.value = TestResult.Idle
        _pairingCode.value = ""
        _pairingPin.value = ""
        _username.value = null
    }

    fun setBaseUrl(url: String) {
        _baseUrl.value = url
        _testState.value = TestResult.Idle
    }

    fun setPairingCode(code: String) {
        _pairingCode.value = code
        _testState.value = TestResult.Idle
    }

    fun setPairingPin(pin: String) {
        _pairingPin.value = pin
        _testState.value = TestResult.Idle
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
                    val fallbackPairUrl = "https://vela.mikesplore.tech/pair/complete"
                    completePairing(fallbackPairUrl, code, pin, "https://vela.mikesplore.tech")
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
            _testState.value = TestResult.Error("Please fill all pairing fields.")
            return
        }

        val pairUrl = if (url.endsWith("/")) "${url}pair/complete" else "$url/pair/complete"
        completePairing(pairUrl, code, pin, url)
    }

    private fun completePairing(pairUrl: String, code: String, pin: String, vpsUrl: String?) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _testState.value = TestResult.Testing("Pairing with relay...")
            when (val result = pairDeviceUseCase(pairUrl, code, pin, vpsUrl = vpsUrl)) {
                is Resource.Success -> {
                    val username = result.data.username ?: "user"
                    _username.value = username
                    _testState.value = TestResult.Success(username)
                    _pairingComplete.value = true
                }
                is Resource.Error -> {
                    _testState.value = TestResult.Error(result.message)
                    appEventManager.showActionErrorSnackbar(result.message)
                }
                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }

    private var capsJob: kotlinx.coroutines.Job? = null

    fun loadCapabilities() {
        capsJob?.cancel()
        capsJob = viewModelScope.launch {
            _capabilitiesState.value = CapabilitiesLoadState.Loading
            var attempt = 0
            while (true) {
                attempt++
                when (val result = capabilitiesRepository.fetchCapabilities(refreshProbes = false)) {
                    is Resource.Success -> {
                        val count = result.data.modules.count { it.value.available }
                        _availableModuleCount.value = count
                        _capabilitiesState.value = CapabilitiesLoadState.Success(count)
                        pushRegistrar.registerIfPossible()
                        return@launch
                    }
                    is Resource.Error -> {
                        _capabilitiesState.value = CapabilitiesLoadState.Error(
                            (result.message.ifBlank { "Failed to load capabilities" }) +
                                if (attempt > 1) " (attempt $attempt)" else ""
                        )
                        delay((1500L * attempt).coerceAtMost(8000L))
                        _capabilitiesState.value = CapabilitiesLoadState.Loading
                    }
                    else -> delay(1000)
                }
            }
        }
    }

    fun finishOnboarding() {
        _pairingComplete.value = true
    }
}
