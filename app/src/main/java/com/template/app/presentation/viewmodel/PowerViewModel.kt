package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PowerState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val currentProfile: String? = null
)

@HiltViewModel
class PowerViewModel @Inject constructor(
    private val velaRepository: VelaRepository
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
            _state.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            when (val result = velaRepository.setPowerProfile(profile)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, successMessage = "Power profile set to $profile", currentProfile = profile) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
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
            _state.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            when (val result = action()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, successMessage = "$actionName signal sent to host") }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
