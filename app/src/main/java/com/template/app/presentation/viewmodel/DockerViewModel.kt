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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
            _ui.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectContainer(id: String) {
        _ui.update { it.copy(selectedId = id, detail = null, logs = null) }
        viewModelScope.launch {
            when (val detail = repository.getContainer(id)) {
                is Resource.Success -> _ui.update { it.copy(detail = detail.data) }
                is Resource.Error -> appEventManager.showActionErrorSnackbar(detail.message)
                else -> {}
            }
            when (val logs = repository.getLogs(id, lines = 100)) {
                is Resource.Success -> _ui.update { it.copy(logs = logs.data) }
                is Resource.Error -> { /* detail still useful without logs */ }
                else -> {}
            }
        }
    }

    fun clearSelection() {
        _ui.update { it.copy(selectedId = null, detail = null, logs = null) }
    }

    fun startSelected() = runAction { repository.start(it) }
    fun stopSelected() = runAction { repository.stop(it) }
    fun restartSelected() = runAction { repository.restart(it) }

    private fun runAction(block: suspend (String) -> Resource<String>) {
        val id = _ui.value.selectedId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(actionBusy = true) }
            when (val result = block(id)) {
                is Resource.Success -> {
                    appEventManager.showActionSuccessSnackbar(result.data)
                    repository.refreshContainers(all = true, filter = _ui.value.filter.ifBlank { null })
                    repository.refreshInfo()
                    selectContainer(id)
                }
                is Resource.Error -> appEventManager.showActionErrorSnackbar(result.message)
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
