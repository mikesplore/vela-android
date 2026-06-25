package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.AppEventManager
import com.template.app.core.utils.Resource
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.repository.AssistantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantState(
    val messages: List<AssistantChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoading: Boolean = true,
    val inputText: String = ""
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val appEventManager: AppEventManager
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        repository.observeMessages()
            .onEach { messages ->
                _state.update { it.copy(
                    messages = messages,
                    isInitialLoading = false
                ) }
            }
            .launchIn(viewModelScope)
    }

    fun onInputTextChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isEmpty() || _state.value.isLoading) return

        if (text.lowercase() == "clear") {
            _state.update { it.copy(inputText = "") }
            clearChat()
            return
        }

        repository.sendMessageStream(text)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(inputText = "", isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                    is Resource.Error -> {
                        appEventManager.showActionErrorSnackbar(result.message)
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun confirmAction(confirmed: Boolean) {
        val message = if (confirmed) "yes" else "cancel"
        _state.update { it.copy(inputText = message) }
        sendMessage()
    }

    fun submitPin(pin: String) {
        _state.update { it.copy(inputText = pin) }
        sendMessage()
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }
}
