package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.VelaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClipboardState(
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false
)

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val repository: VelaRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(ClipboardState())
    val state = _state.asStateFlow()

    init {
        observeClipboard()
        refresh()
    }

    private fun observeClipboard() {
        repository.observeClipboard()
            .onEach { clipboard ->
                _state.update { it.copy(content = clipboard?.content ?: "") }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.readClipboard()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                }

                is Resource.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                    // Only show snackbar if user explicitly refreshed (optional, keeping it quiet for init)
                }

                else -> {}
            }
        }
    }

    fun writeClipboard(text: String) {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.writeClipboard(text)) {
                is Resource.Success -> {
                    _state.update { it.copy(isUpdating = false) }
                    appEventManager.showActionSuccessSnackbar("Clipboard updated")
                }

                is Resource.Error -> {
                    _state.update { it.copy(isUpdating = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }

    fun clearClipboard() {
        viewModelScope.launch {
            appEventManager.setLoading(true)
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.clearClipboard()) {
                is Resource.Success -> {
                    _state.update { it.copy(isUpdating = false) }
                    appEventManager.showActionSuccessSnackbar("Clipboard cleared")
                }

                is Resource.Error -> {
                    _state.update { it.copy(isUpdating = false) }
                    appEventManager.showActionErrorSnackbar(result.message)
                }

                else -> {}
            }
            appEventManager.setLoading(false)
        }
    }
}