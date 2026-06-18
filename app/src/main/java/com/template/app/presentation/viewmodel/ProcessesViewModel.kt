package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.domain.model.VelaProcess
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    private val velaRepository: VelaRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessesState())
    val state = _state.asStateFlow()

    private val _limit = MutableStateFlow(10)

    init {
        observeData()
        refresh()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        velaRepository.observeActiveWindow()
            .onEach { window -> _state.update { it.copy(activeWindow = window) } }
            .launchIn(viewModelScope)

        _limit.flatMapLatest { limit ->
            velaRepository.observeProcesses(limit)
        }.onEach { list ->
            _state.update { it.copy(processes = list, currentLimit = _limit.value) }
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSortChanged(sortType: ProcessesSortType) {
        _state.update { it.copy(sortBy = sortType) }
    }

    fun loadMore() {
        if (_state.value.isLoading) return
        _limit.value += 10
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
