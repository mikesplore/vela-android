package com.template.app.presentation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.components.DashboardFabMenu
import com.template.app.presentation.ui.components.DeviceSwitcherSheet
import com.template.app.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAddDevice: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showDeviceSwitcher by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.setFabVisible(true)
        onDispose {
            viewModel.setFabVisible(false)
        }
    }

    if (showDeviceSwitcher) {
        DeviceSwitcherSheet(
            devices = state.pairedDevices,
            onDismiss = { showDeviceSwitcher = false },
            onSwitch = { viewModel.switchDevice(it) },
            onAddDevice = onAddDevice
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(onClick = { showDeviceSwitcher = true }) {
                        Icon(
                            Icons.Default.Devices,
                            contentDescription = "Switch device",
                            modifier = Modifier.size(18.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.activeDevice?.displayName ?: "Device",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        floatingActionButton = {
            DashboardFabMenu(
                isExpanded = isFabMenuExpanded,
                onToggle = { isFabMenuExpanded = !isFabMenuExpanded },
                onScreenshot = { viewModel.takeScreenshot(); },
                onLock = { viewModel.lockScreen(); },
                onPlayPause = { viewModel.togglePlayPause(); }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                StatusCard(
                    uptime = state.uptime
                )
            }

            state.network?.let { network ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 60)) + slideInVertically(
                        tween(450, delayMillis = 60)
                    ) { it / 4 }
                ) {
                    NetworkCard(network, state.wifi)
                }
            }

            state.resolution?.let { resolution ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 120)) + slideInVertically(
                        tween(450, delayMillis = 120)
                    ) { it / 4 }
                ) {
                    SystemResolutionCard(resolution)
                }
            }

            if (state.processes.isNotEmpty() || !state.activeWindow.isNullOrBlank()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 180)) + slideInVertically(
                        tween(450, delayMillis = 180)
                    ) { it / 4 }
                ) {
                    ProcessSummaryCard(
                        processes = state.processes,
                        activeWindow = state.activeWindow,
                        currentLimit = state.processLimit,
                        cpuUsage = state.cpuUsage,
                        ramUsage = state.ramUsage,
                        onToggleLimit = { viewModel.toggleProcessLimit() }
                    )
                }
            }

            state.audio?.let { audio ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 240)) + slideInVertically(
                        tween(450, delayMillis = 240)
                    ) { it / 4 }
                ) {
                    AudioControlCard(
                        audioState = audio,
                        onVolumeChange = { viewModel.setVolume(it) },
                        onMuteToggle = { viewModel.setMute(it) }
                    )
                }
            }

            if (state.isConnected) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 300)) + slideInVertically(
                        tween(450, delayMillis = 300)
                    ) { it / 4 }
                ) {
                    BrightnessControlCard(
                        brightness = state.brightness,
                        onBrightnessChange = { viewModel.setBrightness(it) }
                    )
                }
            }

            if (state.disks.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 360)) + slideInVertically(
                        tween(450, delayMillis = 360)
                    ) { it / 4 }
                ) {
                    DiskUsageCard(state.disks)
                }
            }

            state.media?.let { media ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(450, delayMillis = 420)) + slideInVertically(
                        tween(450, delayMillis = 420)
                    ) { it / 4 }
                ) {
                    MediaBar(
                        media = media,
                        onTogglePlayPause = { viewModel.togglePlayPause() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (state.isScreenshotLoading || state.screenshot != null) {
        ScreenshotSheet(
            bitmap = state.screenshot,
            isLoading = state.isScreenshotLoading,
            onDismiss = { viewModel.dismissScreenshot() }
        )
    }
}
