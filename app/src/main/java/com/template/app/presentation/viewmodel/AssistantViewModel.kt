package com.template.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.app.domain.model.AssistantSendEvent
import com.template.app.domain.model.AssistantSendPhase
import com.template.app.domain.model.SecureReplyKind
import com.template.app.domain.repository.AssistantRepository
import com.template.app.domain.repository.CapabilitiesRepository
import com.template.app.domain.repository.SettingsRepository
import com.template.app.domain.usecase.GetSettingsUseCase
import com.template.app.presentation.ui.chat.AssistantToolSuggestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val inputText: String = "",
    val sendPhase: AssistantSendPhase = AssistantSendPhase.Idle,
    val suggestions: List<com.template.app.presentation.ui.chat.AssistantSuggestion> = emptyList()
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
    private val settingsRepository: SettingsRepository,
    private val capabilitiesRepository: CapabilitiesRepository,
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    val biometricsEnabled = getSettingsUseCase()
        .map { it.biometricsEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        observeMessages()
        refreshAssistantTools()
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

    /** Always refresh capabilities tools when chat launches; pick 4 mapped suggestions. */
    fun refreshAssistantTools() {
        viewModelScope.launch {
            capabilitiesRepository.fetchCapabilities(refreshProbes = false)
        }
        capabilitiesRepository.observeAvailableAssistantTools()
            .distinctUntilChanged()
            .onEach { tools ->
                _state.update {
                    it.copy(suggestions = AssistantToolSuggestions.pickRandom(tools, count = 4))
                }
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
        _state.update {
            it.copy(
                inputText = "",
                isLoading = true,
                sendPhase = AssistantSendPhase.Preparing
            )
        }

        repository.sendMessageStream(wireMessage, secureReplyKind)
            .onEach { event ->
                when (event) {
                    is AssistantSendEvent.Phase -> {
                        _state.update { it.copy(isLoading = true, sendPhase = event.phase) }
                    }
                    is AssistantSendEvent.Finished -> {
                        _state.update {
                            it.copy(isLoading = false, sendPhase = AssistantSendPhase.Idle)
                        }
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
