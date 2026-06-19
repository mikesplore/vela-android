package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.model.VelaProcess
import com.template.app.domain.repository.ProcessesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessesState(
    val processes: List<VelaProcess> = emptyList(),
    val activeWindow: String? = null,
    val searchQuery: String = "",
    val sortBy: ProcessesSortType = ProcessesSortType.CPU,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentLimit: Int = 10
)

enum class ProcessesSortType {
    CPU, MEM
}

@HiltViewModel
class ProcessesViewModel @Inject constructor(
    private val velaRepository: ProcessesRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessesState())
    val state = _state.asStateFlow()

    private val _limit = MutableStateFlow(10)

    init {
        observeData()
        refresh()
    }

    // Inside ProcessesViewModel.kt -> observeData()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        velaRepository.observeActiveWindow()
            .onEach { window -> _state.update { it.copy(activeWindow = window) } }
            .launchIn(viewModelScope)

        combine(
            _limit.flatMapLatest { velaRepository.observeProcesses(it) },
            _state.map { it.sortBy }.distinctUntilChanged(),
            _state.map { it.searchQuery }.distinctUntilChanged()
        ) { processes, sortBy, query ->
            processes
                .filter { it.name.contains(query, ignoreCase = true) }
                .let { list ->
                    when (sortBy) {
                        ProcessesSortType.CPU -> list.sortedByDescending { it.cpu }
                        ProcessesSortType.MEM -> list.sortedByDescending { it.mem }
                    }
                }
        }.onEach { sortedList ->
            // FIX: Use .update and only change processes/limit,
            // do not touch isLoading here so loadMore() can control it
            _state.update {
                it.copy(
                    processes = sortedList,
                    currentLimit = _limit.value
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSortChanged(sortType: ProcessesSortType) {
        _state.update { it.copy(sortBy = sortType) }
    }

    fun loadMore() {
        // Prevent multiple simultaneous loads
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Increment the limit which triggers the flatMapLatest in observeData
                _limit.value += 10

                // CRITICAL: Explicitly tell the repository to fetch more data
                // if it's a one-shot fetch that feeds a Room/Local flow
                velaRepository.getProcesses()
            } catch (e: Exception) {
                appEventManager.showActionErrorSnackbar("Failed to load more: ${e.message}")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun killProcess(pid: Int) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            try {
                velaRepository.killProcess(pid)
                appEventManager.showActionSuccessSnackbar("Process killed successfully")
                refresh()
            } catch (e: Exception) {
                appEventManager.showActionErrorSnackbar("Failed to kill process: ${e.message}")
            } finally {
                appEventManager.setLoading(false)
            }
        }
    }


    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                velaRepository.getProcesses()
                velaRepository.getActiveWindow()
            } catch (e: Exception) {
                appEventManager.showActionErrorSnackbar("Failed to refresh: ${e.message}")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
