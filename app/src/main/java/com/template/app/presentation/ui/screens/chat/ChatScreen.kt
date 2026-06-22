package com.template.app.presentation.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(    onBack: () -> Unit,
                   viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // reverseLayout = true means index 0 is at the bottom
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Simplified list: reversed for better keyboard behavior
    val displayMessages = remember(state.messages) {
        state.messages.reversed()
    }

    // Auto-scroll only needed when a NEW message is sent by user
    LaunchedEffect(state.messages.size) {
        if (state.messages.lastOrNull()?.isUser == true) {
            listState.animateScrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // Apply padding to the whole screen container
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.isInitialLoading) {
                    // Show a loader or nothing, but NOT the empty state yet
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.messages.isEmpty()) {
                    // This now only shows if we are confirmed empty
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
                        reverseLayout = true, // Key for chat UIs
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom)
                    ) {
                        // If typing, show indicator at the bottom (index 0 in reverse)
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
                                onConfirm = { viewModel.confirmAction(true) },
                                onCancel = { viewModel.confirmAction(false) },
                                onPinSubmit = { viewModel.submitPin(it) },
                                modifier = Modifier.animateItem()
                            )
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
                // .imePadding() removed here because it's on the root Box now
            )
        }

        // Scroll to bottom button logic reversed
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
}
