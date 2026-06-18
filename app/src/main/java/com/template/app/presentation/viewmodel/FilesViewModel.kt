package com.template.app.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaBreadcrumb
import com.template.app.domain.model.VelaDiskUsage
import com.template.app.domain.model.VelaFileInfo
import com.template.app.domain.repository.VelaRepository
import com.template.app.domain.usecase.ObserveVelaConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FilesState(
    val currentPath: String = "",
    val parentPath: String? = null,
    val files: List<VelaFileInfo> = emptyList(),
    val disks: List<VelaDiskUsage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isUploading: Boolean = false,
    val isPerformingAction: Boolean = false,
    val showHidden: Boolean = false,
    val breadcrumbs: List<VelaBreadcrumb> = emptyList(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: VelaRepository,
    private val observeVelaConfigUseCase: ObserveVelaConfigUseCase,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _currentPath = MutableStateFlow<String?>(null)
    
    private val _state = MutableStateFlow(FilesState())
    val state: StateFlow<FilesState> = _state.asStateFlow()

    init {
        loadDisks()
        
        repository.observeDisks()
            .onEach { disks -> _state.update { it.copy(disks = disks) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            observeVelaConfigUseCase()
                .filterNotNull()
                .firstOrNull()?.let { config ->
                    if (_currentPath.value == null) {
                        loadFiles(config.homeDirectory)
                    }
                }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        _currentPath
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { path -> 
                Log.d("FilesViewModel", "Observing path: $path")
                repository.observeFiles(path) 
            }
            .onEach { files -> 
                _state.update { it.copy(files = files) } 
            }
            .launchIn(viewModelScope)
    }

    fun loadFiles(path: String?, showHidden: Boolean = _state.value.showHidden) {
        val requestedPath = path ?: ""
        val isFirstLoad = _state.value.files.isEmpty() || requestedPath != _currentPath.value

        _state.update {
            it.copy(
                isLoading = isFirstLoad,
                isRefreshing = !isFirstLoad,
                error = null
            )
        }

        _currentPath.value = requestedPath

        viewModelScope.launch {
            val result = repository.listFiles(path, showHidden)
            if (result is Resource.Success) {
                val data = result.data
                val actualPath = data.currentPath

                if (actualPath != _currentPath.value) {
                    _currentPath.value = actualPath
                }

                _state.update { it.copy(
                    currentPath = actualPath,
                    parentPath = data.parentPath,
                    showHidden = data.showHidden
                ) }

                updateBreadcrumbs(actualPath)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private suspend fun updateBreadcrumbs(path: String) {
        if (path.isBlank()) {
            _state.update { it.copy(breadcrumbs = emptyList()) }
            return
        }
        val result = repository.getFileTree(path, maxDepth = 1)
        if (result is Resource.Success) {
            _state.update { it.copy(breadcrumbs = result.data.breadcrumbs) }
        }
    }

    private fun loadDisks() {
        viewModelScope.launch {
            repository.getDiskUsage()
        }
    }

    fun navigateTo(path: String) {
        loadFiles(path)
    }

    fun navigateUp() {
        val parent = _state.value.parentPath
        if (parent != null) {
            loadFiles(parent)
        } else {
            val current = _state.value.currentPath
            if (current.isNotBlank() && current != "/") {
                val manualParent = current.substringBeforeLast("/", "").ifBlank { "/" }
                loadFiles(manualParent)
            } else if (current.isNotBlank()) {
                loadFiles(null)
            }
        }
    }

    fun toggleShowHidden() {
        loadFiles(_state.value.currentPath, !_state.value.showHidden)
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun createDirectory(name: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val current = _state.value.currentPath
            val path = if (current.isEmpty()) name else if (current.endsWith("/")) "$current$name" else "$current/$name"
            val result = repository.makeDirectory(path)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("Directory created")
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }

    fun deleteFile(file: VelaFileInfo) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val result = repository.deleteFile(file.path)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("File deleted")
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }

    fun renameFile(file: VelaFileInfo, newName: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val parentPath = file.path.substringBeforeLast("/", "")
            val newPath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
            val result = repository.renameFile(file.path, newPath)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("File renamed")
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }

    fun uploadFile(localFile: File) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val result = repository.uploadFile(_state.value.currentPath, localFile)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("File uploaded")
                loadFiles(_currentPath.value)
                localFile.delete()
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }

    fun openFile(file: VelaFileInfo) {
        viewModelScope.launch {
            repository.openFile(file.path)
        }
    }

    fun zipFiles(files: List<VelaFileInfo>, outputName: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val paths = files.map { it.path }
            val current = _state.value.currentPath
            val outputPath = if (current.isEmpty()) outputName else if (current.endsWith("/")) "$current$outputName" else "$current/$outputName"
            val result = repository.zipFiles(paths, outputPath)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("Files zipped")
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }

    fun unzipFile(file: VelaFileInfo) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            val destination = file.path.removeSuffix(".zip")
            val result = repository.unzipFile(file.path, destination)
            if (result is Resource.Success) {
                appEventManager.showActionSuccessSnackbar("File unzipped")
                loadFiles(_currentPath.value)
            } else if (result is Resource.Error) {
                appEventManager.showActionErrorSnackbar(result.message)
            }
            appEventManager.setLoading(false)
        }
    }
}
