package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.DockerComposeStatus
import com.template.app.domain.model.DockerContainer
import com.template.app.domain.model.DockerContainerDetail
import com.template.app.domain.model.DockerInfo
import com.template.app.domain.model.DockerLogs
import com.template.app.domain.repository.DockerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DockerUiState(
    val isRefreshing: Boolean = false,
    val filter: String = "",
    val selectedId: String? = null,
    val detail: DockerContainerDetail? = null,
    val logs: DockerLogs? = null,
    val compose: DockerComposeStatus? = null,
    val composeProject: String = "",
    val composeDirectory: String = "",
    val actionBusy: Boolean = false
)

@HiltViewModel
class DockerViewModel @Inject constructor(
    private val repository: DockerRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    val info = repository.observeInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null as DockerInfo?)

    val containers = repository.observeContainers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<DockerContainer>())

    private val _ui = MutableStateFlow(DockerUiState())
    val ui = _ui.asStateFlow()

    init {
        refreshAll()
        
        // Use combine to keep detail in sync with the live container list
        viewModelScope.launch {
            combine(containers, _ui) { list, state ->
                Pair(list, state.selectedId)
            }.collectLatest { (list, selectedId) ->
                if (selectedId == null) return@collectLatest
                val matching = list.find { it.id == selectedId } ?: return@collectLatest
                
                _ui.update { state ->
                    val currentDetail = state.detail ?: return@update state
                    // Only update if state or status actually changed to avoid unnecessary recompositions
                    if (currentDetail.state != matching.state || currentDetail.status != matching.status) {
                        state.copy(
                            detail = currentDetail.copy(
                                state = matching.state,
                                status = matching.status
                            )
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun setFilter(value: String) {
        _ui.update { it.copy(filter = value) }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _ui.update { it.copy(isRefreshing = true) }
            repository.refreshInfo()
            val filter = _ui.value.filter.ifBlank { null }
            when (val result = repository.refreshContainers(all = true, filter = filter)) {
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> {}
            }
            _ui.value.selectedId?.let { refreshSelected(it) }
            _ui.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectContainer(id: String) {
        if (_ui.value.selectedId != id) {
            _ui.update { it.copy(selectedId = id, detail = null, logs = null) }
        }
        viewModelScope.launch {
            refreshSelected(id)
        }
    }

    private suspend fun refreshSelected(id: String) {
        when (val res = repository.getContainer(id)) {
            is Resource.Success -> {
                _ui.update { state ->
                    // Merge health/ports/etc from detail call, but respect the latest state from the list if available
                    val liveState = containers.value.find { it.id == id }
                    state.copy(
                        detail = res.data.copy(
                            state = liveState?.state ?: res.data.state,
                            status = liveState?.status ?: res.data.status
                        )
                    )
                }
            }
            is Resource.Error -> if (_ui.value.detail == null) appEventManager.showActionErrorSnackbar(res.message)
            else -> {}
        }
        when (val logs = repository.getLogs(id, lines = 100)) {
            is Resource.Success -> _ui.update { it.copy(logs = logs.data) }
            else -> {}
        }
    }

    fun clearSelection() {
        _ui.update { it.copy(selectedId = null, detail = null, logs = null) }
    }

    fun startSelected() = runAction("starting") { repository.start(it) }
    fun stopSelected() = runAction("stopping") { repository.stop(it) }
    fun restartSelected() = runAction("restarting") { repository.restart(it) }

    private fun runAction(optimisticState: String, block: suspend (String) -> Resource<String>) {
        val id = _ui.value.selectedId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(actionBusy = true) }
            
            // Optimistic update
            _ui.update { state ->
                state.copy(detail = state.detail?.copy(state = optimisticState))
            }
            
            when (val result = block(id)) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar(result.data)
                    
                    // Progressive refresh
                    repeat(3) { i ->
                        delay(500L * (i + 1))
                        repository.refreshContainers(all = true, filter = _ui.value.filter.ifBlank { null })
                        repository.refreshInfo()
                        refreshSelected(id)
                    }
                }
                is Resource.Error -> {
                    appEventManager.showActionErrorSnackbar(result.message)
                    refreshSelected(id) // Revert optimistic state
                }
                else -> {}
            }
            _ui.update { it.copy(actionBusy = false) }
        }
    }

    fun setComposeProject(value: String) {
        _ui.update { it.copy(composeProject = value) }
    }

    fun setComposeDirectory(value: String) {
        _ui.update { it.copy(composeDirectory = value) }
    }

    fun loadCompose() {
        viewModelScope.launch {
            val project = _ui.value.composeProject.ifBlank { null }
            val dir = _ui.value.composeDirectory.ifBlank { null }
            when (val result = repository.getCompose(projectDirectory = dir, project = project)) {
                is Resource.Success -> _ui.update { it.copy(compose = result.data) }
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
                else -> {}
            }
        }
    }
}
