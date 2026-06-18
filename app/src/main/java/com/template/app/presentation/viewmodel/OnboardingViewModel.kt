package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaConfig
import com.template.app.domain.repository.VelaRepository
import com.template.app.domain.usecase.CompleteOnboardingUseCase
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val velaRepository: VelaRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _baseUrl = MutableStateFlow("")
    val baseUrl = _baseUrl.asStateFlow()

    private val _apiToken = MutableStateFlow("")
    val apiToken = _apiToken.asStateFlow()

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
                _apiToken.value = settings.apiToken
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

    fun setApiToken(token: String) {
        _apiToken.value = token
        _testState.value = TestResult.Idle
    }

    fun toggleShowPassword() {
        _showPassword.value = !_showPassword.value
    }

    fun testConnection() {
        val urlInput = _baseUrl.value.trim()
        if (urlInput.isEmpty()) {
            _testState.value = TestResult.Error("Please enter a Base URL.")
            return
        }

        viewModelScope.launch {
            appEventManager.setLoading(true)
            _testState.value = TestResult.Testing
            saveSettingsUseCase(urlInput, _apiToken.value.trim())

            when (val result = velaRepository.getConfig()) {
                is Resource.Success -> {
                    val config = result.data
                    _username.value = config.username
                    _testState.value = TestResult.Success(config.username)
                    appEventManager.showActionSuccessSnackbar("Connected as ${config.username}")
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

    fun completeOnboarding(isDemo: Boolean) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            if (isDemo) {
                saveSettingsUseCase("http://demo.vela-agent.local", "demo_secret")
                velaRepository.setConfig(VelaConfig("/home/demo", "Demo User"))
            } else {
                saveSettingsUseCase(_baseUrl.value.trim(), _apiToken.value.trim())
            }
            completeOnboardingUseCase()
            appEventManager.showActionSuccessSnackbar("Setup complete!")
            appEventManager.setLoading(false)
        }
    }
}