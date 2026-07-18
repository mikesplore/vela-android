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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.components.DashboardFabMenu
import com.template.app.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.setFabVisible(true)
        onDispose {
            viewModel.setFabVisible(false)
        }
    }

    Scaffold(
        floatingActionButton = {
            DashboardFabMenu(
                isExpanded = isFabMenuExpanded,
                onToggle = { isFabMenuExpanded = !isFabMenuExpanded },
                onScreenshot = { viewModel.takeScreenshot(); },
                onLock = { viewModel.lockScreen(); },
                onPlayPause = { viewModel.togglePlayPause(); }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

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
