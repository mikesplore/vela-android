package com.template.app.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    val ambientGradientBg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "VELA DASHBOARD",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            DashboardFabMenu(
                isExpanded = isFabMenuExpanded,
                onToggle = { isFabMenuExpanded = !isFabMenuExpanded },
                onScreenshot = { viewModel.takeScreenshot(); isFabMenuExpanded = false },
                onLock = { viewModel.lockScreen(); isFabMenuExpanded = false },
                onPlayPause = { viewModel.togglePlayPause(); isFabMenuExpanded = false }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ambientGradientBg)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.error?.let { msg ->
                    ErrorMessage(msg)
                }

                state.health?.let { health ->
                    StatusCard(
                        health = health,
                        isConnected = state.isConnected,
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.refreshAllData() }
                    )
                }

                state.network?.let { network ->
                    NetworkCard(network)
                }

                if (state.processes.isNotEmpty() || !state.activeWindow.isNullOrBlank()) {
                    ProcessSummaryCard(state.processes, state.activeWindow)
                }

                state.audio?.let { audio ->
                    AudioControlCard(
                        audioState = audio,
                        onVolumeChange = { viewModel.setVolume(it) },
                        onMuteToggle = { viewModel.setMute(it) }
                    )
                }

                if (state.isConnected) {
                    BrightnessControlCard(
                        brightness = state.brightness,
                        onBrightnessChange = { viewModel.setBrightness(it) }
                    )
                }

                if (state.disks.isNotEmpty()) {
                    DiskUsageCard(state.disks)
                }

                state.media?.let { media ->
                    MediaBar(
                        media = media,
                        onTogglePlayPause = { viewModel.togglePlayPause() }
                    )
                }

                ClipboardCard(
                    currentText = state.clipboardText,
                    onWriteText = { viewModel.writeClipboard(it) }
                )

                Spacer(modifier = Modifier.height(80.dp))
            }

            if (state.isRefreshing && state.health == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    state.screenshot?.let { bitmap ->
        ScreenshotDialog(
            bitmap = bitmap,
            onDismiss = { viewModel.dismissScreenshot() }
        )
    }
}

@Composable
fun DashboardFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onScreenshot: () -> Unit,
    onLock: () -> Unit,
    onPlayPause: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFab(onClick = onScreenshot, icon = Icons.Default.PhotoCamera, label = "Screenshot")
                SmallFab(onClick = onLock, icon = Icons.Default.Lock, label = "Lock Screen")
                SmallFab(onClick = onPlayPause, icon = Icons.Default.PlayArrow, label = "Play / Pause")
            }
        }

        FloatingActionButton(onClick = onToggle) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.MenuOpen,
                contentDescription = "Menu"
            )
        }
    }
}

@Composable
fun SmallFab(onClick: () -> Unit, icon: ImageVector, label: String) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ErrorMessage(msg: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.width(10.dp))
            Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
    }
}
