package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.PowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PowerState(
    val isLoading: Boolean = false,
    val currentProfile: String? = null
)

@HiltViewModel
class PowerViewModel @Inject constructor(
    private val velaRepository: PowerRepository,
    private val appEventManager: AppEventManager // Added
) : ViewModel() {

    private val _state = MutableStateFlow(PowerState())
    val state = _state.asStateFlow()

    init {
        refreshPowerProfile()
    }

    fun shutdown() {
        executePowerAction("Shutdown") { velaRepository.shutdown() }
    }

    fun restart() {
        executePowerAction("Restart") { velaRepository.restart() }
    }

    fun sleep() {
        executePowerAction("Sleep") { velaRepository.sleep() }
    }

    fun hibernate() {
        executePowerAction("Hibernate") { velaRepository.hibernate() }
    }

    fun scheduleShutdown(at: String) {
        executePowerAction("Scheduled Shutdown") { velaRepository.scheduleShutdown(at) }
    }

    fun cancelShutdown() {
        executePowerAction("Cancel Scheduled Shutdown") { velaRepository.cancelShutdown() }
    }

    fun setPowerProfile(profile: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isLoading = true) }
            when (val result = velaRepository.setPowerProfile(profile)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, currentProfile = profile) }
                    appEventManager.showActionSuccessSnackbar("Power profile set to $profile")
                }

                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
            appEventManager.setLoading(false)
        }
    }

    fun refreshPowerProfile() {
        viewModelScope.launch {
            when (val result = velaRepository.getPowerProfile()) {
                is Resource.Success -> {
                    _state.update { it.copy(currentProfile = result.data) }
                }

                else -> {}
            }
        }
    }

    private fun executePowerAction(actionName: String, action: suspend () -> Resource<Unit>) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isLoading = true) }
            when (val result = action()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    appEventManager.showActionSuccessSnackbar("$actionName signal sent to host")
                }

                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
            appEventManager.setLoading(false)
        }
    }
}