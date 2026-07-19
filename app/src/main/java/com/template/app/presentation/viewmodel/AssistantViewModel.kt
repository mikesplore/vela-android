package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.core.utils.Resource
import com.template.app.domain.model.SecureReplyKind
import com.template.app.domain.repository.AssistantRepository
import com.template.app.domain.repository.SettingsRepository
import com.template.app.domain.usecase.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantState(
    val messages: List<com.template.app.domain.model.AssistantChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoading: Boolean = true,
    val inputText: String = ""
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val settingsRepository: SettingsRepository,
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    val biometricsEnabled = getSettingsUseCase()
        .map { it.biometricsEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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

        streamMessage(text, secureReplyKind = null)
    }

    fun sendSecureReply(wireMessage: String, kind: SecureReplyKind) {
        if (_state.value.isLoading) return
        streamMessage(wireMessage, kind)
    }

    fun confirmAction(confirmed: Boolean) {
        val message = if (confirmed) "yes" else "cancel"
        val kind = if (confirmed) SecureReplyKind.CONFIRMED else SecureReplyKind.CANCELLED
        sendSecureReply(message, kind)
    }

    fun submitPin(pin: String) {
        sendSecureReply(pin, SecureReplyKind.PIN_VERIFIED)
    }

    fun getStoredPin(): String? = settingsRepository.getStoredPin()

    private fun streamMessage(wireMessage: String, secureReplyKind: SecureReplyKind?) {
        repository.sendMessageStream(wireMessage, secureReplyKind)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(inputText = "", isLoading = true) }
                    }
                    is Resource.Success -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                    is Resource.Error -> {
                        // Error text is persisted as an assistant reply in the transcript
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }
}
