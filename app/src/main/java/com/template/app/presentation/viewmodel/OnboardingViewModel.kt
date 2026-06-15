package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
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
    private val velaRepository: VelaRepository
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

    sealed interface TestResult {
        object Idle : TestResult
        object Testing : TestResult
        data class Success(val uptime: String) : TestResult
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
        if (_currentPage.value < 2) {
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
            _testState.value = TestResult.Testing
            // Temporarily save settings to test connection since VelaRepository 
            // uses the dynamic interceptor which reads from DB
            saveSettingsUseCase(urlInput, _apiToken.value.trim())
            
            when (val result = velaRepository.getHealth()) {
                is Resource.Success -> {
                    val uptimeFormatted = formatUptime(result.data.uptimeSeconds)
                    _testState.value = TestResult.Success(uptimeFormatted)
                }
                is Resource.Error -> {
                    _testState.value = TestResult.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun completeOnboarding(isDemo: Boolean) {
        viewModelScope.launch {
            if (isDemo) {
                saveSettingsUseCase("http://demo.vela-agent.local", "demo_secret")
            } else {
                saveSettingsUseCase(_baseUrl.value.trim(), _apiToken.value.trim())
            }
            completeOnboardingUseCase()
        }
    }

    private fun formatUptime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hrs > 0 -> "${hrs}h ${mins}m ${secs}s"
            mins > 0 -> "${mins}m ${secs}s"
            else -> "${secs}s"
        }
    }
}
