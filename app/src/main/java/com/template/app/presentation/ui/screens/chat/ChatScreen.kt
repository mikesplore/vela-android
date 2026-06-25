package com.template.app.presentation.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.presentation.ui.components.VelaConfirmationSheet
import com.template.app.presentation.ui.components.VelaPinSheet
import com.template.app.presentation.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Sheet States
    var confirmationMessage by remember { mutableStateOf<AssistantChatMessage?>(null) }
    var pinMessage by remember { mutableStateOf<AssistantChatMessage?>(null) }

    // Simplified list: reversed for better keyboard behavior
    val displayMessages = remember(state.messages) {
        state.messages.reversed()
    }

    // Auto-scroll and Sheet Trigger for new messages or loading state changes
    LaunchedEffect(state.messages.size, state.isLoading) {
        if (state.messages.isNotEmpty() || state.isLoading) {
            // Always scroll to bottom (index 0 in reverse layout)
            listState.animateScrollToItem(0)
        }

        // Trigger sheets for the latest assistant message based on required actions
        val lastMessage = state.messages.lastOrNull()
        if (lastMessage != null && !lastMessage.isUser) {
            val isPinRequired = lastMessage.isPinRequired
            val requiresAuth = lastMessage.confirmation?.requiresAuth == true

            when {
                isPinRequired || requiresAuth -> {
                    pinMessage = lastMessage
                    confirmationMessage = null
                }
                lastMessage.confirmation != null -> {
                    confirmationMessage = lastMessage
                    pinMessage = null
                }
                else -> {
                    confirmationMessage = null
                    pinMessage = null
                }
            }
        } else {
            // Clear sheets when user is typing or last message is from user
            confirmationMessage = null
            pinMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.isInitialLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.messages.isEmpty()) {
                    EmptyState(
                        onSuggestionClick = { suggestion ->
                            viewModel.onInputTextChanged(suggestion)
                            viewModel.sendMessage()
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom)
                    ) {
                        // In reverseLayout = true, items defined first appear at the bottom
                        if (state.isLoading) {
                            item(key = "typing-indicator") {
                                TypingIndicator()
                            }
                        }

                        itemsIndexed(
                            items = displayMessages,
                            key = { _, item -> item.id }
                        ) { _, message ->
                            MessageBubble(
                                message = message,
                                modifier = Modifier.animateItem()
                            )
                        }

                        item(key = "privacy-disclaimer") {
                            PrivacyDisclaimer()
                        }
                    }
                }
            }

            ChatInputBar(
                text = state.inputText,
                onTextChanged = viewModel::onInputTextChanged,
                onSend = viewModel::sendMessage,
                isLoading = state.isLoading,
                modifier = Modifier.navigationBarsPadding()
            )
        }

        // Scroll to bottom button
        val showScrollToBottom by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 2 }
        }

        AnimatedVisibility(
            visible = showScrollToBottom,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            ScrollToBottomButton {
                coroutineScope.launch { listState.animateScrollToItem(0) }
            }
        }
    }

    // Confirmation Sheet
    confirmationMessage?.let { msg ->
        msg.confirmation?.let { conf ->
            VelaConfirmationSheet(
                onDismiss = { confirmationMessage = null },
                onConfirm = {
                    viewModel.confirmAction(true)
                    confirmationMessage = null
                },
                onCancel = {
                    viewModel.confirmAction(false)
                    confirmationMessage = null
                },
                title = conf.title,
                message = conf.description,
                details = conf.details,
                confirmText = "Confirm",
                dismissText = "Cancel"
            )
        }
    }

    // PIN Sheet
    pinMessage?.let {
        VelaPinSheet(
            onDismiss = { pinMessage = null },
            onSubmit = { pin ->
                viewModel.submitPin(pin)
                pinMessage = null
            }
        )
    }
}
