package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.push.PushRegistrar
import com.template.app.core.utils.Resource
import com.template.app.domain.model.HostCapabilities
import com.template.app.domain.repository.CapabilitiesRepository
import com.template.app.domain.usecase.ObserveActiveDeviceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CapabilitiesViewModel @Inject constructor(
    private val capabilitiesRepository: CapabilitiesRepository,
    private val pushRegistrar: PushRegistrar,
    observeActiveDeviceUseCase: ObserveActiveDeviceUseCase
) : ViewModel() {

    val capabilities = capabilitiesRepository.observeCapabilities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null as HostCapabilities?)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var ensureJob: Job? = null

    init {
        viewModelScope.launch {
            observeActiveDeviceUseCase().collectLatest { device ->
                if (device != null) {
                    ensureCapabilitiesLoaded()
                }
            }
        }
    }

    fun ensureCapabilitiesLoaded() {
        ensureJob?.cancel()
        ensureJob = viewModelScope.launch {
            if (capabilitiesRepository.hasCachedCapabilities()) {
                _error.value = null
                // Soft refresh in background
                launch {
                    capabilitiesRepository.fetchCapabilities(refreshProbes = false)
                    pushRegistrar.registerIfPossible()
                }
                return@launch
            }
            _isLoading.value = true
            _error.value = null
            var attempt = 0
            while (true) {
                attempt++
                when (val result = capabilitiesRepository.fetchCapabilities(refreshProbes = false)) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _error.value = null
                        pushRegistrar.registerIfPossible()
                        return@launch
                    }
                    is Resource.Error -> {
                        _error.value = result.message.ifBlank { "Failed to load capabilities" }
                        delay((1500L * attempt).coerceAtMost(8000L))
                    }
                    else -> delay(1000)
                }
            }
        }
    }

    fun refreshFromSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = capabilitiesRepository.refreshAndFetch()) {
                is Resource.Success -> {
                    _isLoading.value = false
                    pushRegistrar.registerIfPossible()
                }
                is Resource.Error -> {
                    // Fall back to plain fetch with retry loop
                    _error.value = result.message
                    _isLoading.value = false
                    ensureCapabilitiesLoaded()
                }
                else -> _isLoading.value = false
            }
        }
    }
}
