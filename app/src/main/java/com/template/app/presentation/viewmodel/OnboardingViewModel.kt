package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.ConfigRepository
import com.template.app.domain.repository.PairingRepository
import com.template.app.domain.repository.SettingsRepository
import com.template.app.domain.usecase.CompleteOnboardingUseCase
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val configRepository: ConfigRepository,
    private val pairingRepository: PairingRepository,
    private val settingsRepository: SettingsRepository,
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

    sealed interface TestResult {
        object Idle : TestResult
        object Testing : TestResult
        data class Success(val username: String) : TestResult
        data class Error(val message: String) : TestResult
    }

    init {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                _baseUrl.value = settings.baseUrl
            }
        }
    }

    fun nextPage() {
        if (_currentPage.value < 3) {
            _currentPage.value++
            _testState.value = TestResult.Idle
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            _testState.value = TestResult.Idle
        }
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
            val pairUrl = json.optString("pair_url")
            val vpsUrl = json.optString("vps_url")
            val code = json.optString("pairing_code")
            val pin = json.optString("pairing_pin")

            if (pairUrl.isNotEmpty() && code.isNotEmpty() && pin.isNotEmpty()) {
                if (vpsUrl.isNotEmpty()) {
                    _baseUrl.value = vpsUrl
                }
                _pairingCode.value = code
                _pairingPin.value = pin
                completePairing(pairUrl, code, pin)
                return
            }
        } catch (e: Exception) {
            // Not a JSON or missing fields
        }

        try {
            val uri = scannedValue.toUri()
            if (uri.scheme == "vela" && uri.host == "pair") {
                val code = uri.getQueryParameter("code")
                val pin = uri.getQueryParameter("pin")
                if (code != null && pin != null) {
                    val fallbackPairUrl = "https://vela.mikesplore.tech/pair/complete"
                    _pairingCode.value = code
                    _pairingPin.value = pin
                    completePairing(fallbackPairUrl, code, pin)
                }
            }
        } catch (e: Exception) {
            // Invalid URI
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
        completePairing(pairUrl, code, pin)
    }

    private fun completePairing(pairUrl: String, code: String, pin: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _testState.value = TestResult.Testing
            when (val result = pairingRepository.completePairing(pairUrl, code, pin)) {
                is Resource.Success -> {
                    val data = result.data
                    
                    // SAVE relay_secret as apiToken for future requests
                    saveSettingsUseCase(data.relayBaseUrl, data.relaySecret)
                    
                    // After saving, verify config to get username
                    when (val configResult = configRepository.getConfig()) {
                        is Resource.Success -> {
                            _username.value = configResult.data.username
                            _testState.value = TestResult.Success(configResult.data.username)
                            completeOnboarding()
                        }
                        is Resource.Error -> {
                            _testState.value = TestResult.Error(configResult.message)
                            appEventManager.showActionErrorSnackbar(configResult.message)
                        }
                        else -> {}
                    }
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

    fun completeOnboarding() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            completeOnboardingUseCase()
            appEventManager.setLoading(false)
        }
    }
}
