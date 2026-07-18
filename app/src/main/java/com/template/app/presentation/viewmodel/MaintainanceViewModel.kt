package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaPackageUpdate
import com.template.app.domain.model.VelaService
import com.template.app.domain.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaintenanceUiState(
    val visibleServices: List<VelaService> = emptyList(),
    val totalServiceCount: Int = 0,
    val matchedCount: Int = 0,
    val searchQuery: String = "",
    val canLoadMore: Boolean = false,
    val availableUpdates: List<VelaPackageUpdate> = emptyList(),
    val updateManager: String = "",
    val expandedService: String? = null,
    val serviceLogs: List<String> = emptyList(),
    val isLoadingLogs: Boolean = false,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val repository: MaintenanceRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val visibleLimit = MutableStateFlow(PAGE_SIZE)

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    init {
        // UI page: Room query with search + growing LIMIT (load more).
        combine(searchQuery, visibleLimit) { query, limit -> query to limit }
            .flatMapLatest { (query, limit) ->
                repository.observeServices(query = query, limit = limit)
            }
            .onEach { services ->
                _uiState.update { state ->
                    state.copy(
                        visibleServices = services,
                        canLoadMore = services.size < state.matchedCount
                    )
                }
            }
            .launchIn(viewModelScope)

        // Match count always comes from the full DB cache for the current query.
        searchQuery
            .flatMapLatest { query -> repository.observeMatchedServiceCount(query) }
            .onEach { matched ->
                _uiState.update { state ->
                    state.copy(
                        matchedCount = matched,
                        canLoadMore = state.visibleServices.size < matched
                    )
                }
            }
            .launchIn(viewModelScope)

        repository.observeServiceCount()
            .onEach { count ->
                _uiState.update { it.copy(totalServiceCount = count) }
            }
            .launchIn(viewModelScope)

        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Network: pull the full service list into Room.
            loadServices()
            loadUpdates()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
        visibleLimit.value = PAGE_SIZE
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Reveal the next page from Room (does not call the network). */
    fun loadMore() {
        if (!_uiState.value.canLoadMore) return
        visibleLimit.update { it + PAGE_SIZE }
    }

    fun toggleServiceExpanded(name: String) {
        val currentlyExpanded = _uiState.value.expandedService
        if (currentlyExpanded == name) {
            _uiState.update {
                it.copy(expandedService = null, serviceLogs = emptyList(), isLoadingLogs = false)
            }
        } else {
            _uiState.update {
                it.copy(expandedService = name, serviceLogs = emptyList(), isLoadingLogs = true)
            }
            fetchLogs(name)
        }
    }

    private suspend fun loadServices() {
        when (val result = repository.getServices()) {
            is Resource.Error -> {
                _uiState.update { it.copy(error = result.message) }
                appEventManager.showActionErrorSnackbar(result.message)
            }

            else -> Unit
        }
    }

    fun startService(name: String) = performServiceAction(name) { repository.startService(name) }
    fun stopService(name: String) = performServiceAction(name) { repository.stopService(name) }
    fun restartService(name: String) = performServiceAction(name) { repository.restartService(name) }

    private fun performServiceAction(serviceName: String, action: suspend () -> Resource<Unit>) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = action()) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar("Service updated")
                    loadServices()
                    if (_uiState.value.expandedService == serviceName) {
                        fetchLogs(serviceName)
                    }
                }

                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = repository.clearCache()) {
                is Resource.Success -> appEventManager.showActionSuccessSnackbar("Cache cleared")
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    fun syncTime() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            when (val result = repository.syncTime()) {
                is Resource.Success -> appEventManager.showActionSuccessSnackbar("Time sync enabled")
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> Unit
            }
            appEventManager.setLoading(false)
        }
    }

    private suspend fun loadUpdates() {
        when (val result = repository.checkUpdates()) {
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        availableUpdates = result.data.packages,
                        updateManager = result.data.manager
                    )
                }
            }

            else -> Unit
        }
    }

    fun runUpdates() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = repository.runUpdates()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(availableUpdates = emptyList(), isUpdating = false) }
                    appEventManager.showActionSuccessSnackbar("System updated")
                    loadUpdates()
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> _uiState.update { it.copy(isUpdating = false) }
            }
            appEventManager.setLoading(false)
        }
    }

    fun refreshLogs() {
        _uiState.value.expandedService?.let { fetchLogs(it) }
    }

    private fun fetchLogs(service: String) {
        if (service.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLogs = true) }
            when (val result = repository.getLogs(service, LOG_LINES)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(serviceLogs = result.data.lines, isLoadingLogs = false)
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(serviceLogs = emptyList(), isLoadingLogs = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> _uiState.update { it.copy(isLoadingLogs = false) }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 5
        private const val LOG_LINES = 40
    }
}
