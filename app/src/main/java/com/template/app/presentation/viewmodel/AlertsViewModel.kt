package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.AlertHistory
import com.template.app.domain.repository.AlertsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val history: AlertHistory? = null,
    val error: String? = null
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertsRepository: AlertsRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = it.history == null,
                    isRefreshing = it.history != null,
                    error = null
                )
            }
            when (val result = alertsRepository.getHistory(limit = 50, sinceMinutes = 60 * 24 * 7)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            history = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                    appEventManager.showActionErrorSnackbar(result.message)
                }
                else -> {
                    _state.update { it.copy(isLoading = false, isRefreshing = false) }
                }
            }
        }
    }
}
