package com.template.app.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val colorScheme = MaterialTheme.colorScheme


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        state.health?.let { health ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                StatusCard(
                    health = health
                )
            }
        }

        state.network?.let { network ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(450, delayMillis = 60)) + slideInVertically(
                    tween(
                        450,
                        delayMillis = 60
                    )
                ) { it / 4 }
            ) {
                NetworkCard(network, state.wifi)
            }
        }

        state.resolution?.let { resolution ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(450, delayMillis = 120)) + slideInVertically(
                    tween(
                        450,
                        delayMillis = 120
                    )
                ) { it / 4 }
            ) {
                SystemResolutionCard(resolution)
            }
        }

        if (state.processes.isNotEmpty() || !state.activeWindow.isNullOrBlank()) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(450, delayMillis = 180)) + slideInVertically(
                    tween(
                        450,
                        delayMillis = 180
                    )
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
                    tween(
                        450,
                        delayMillis = 240
                    )
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
                    tween(
                        450,
                        delayMillis = 300
                    )
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
                    tween(
                        450,
                        delayMillis = 360
                    )
                ) { it / 4 }
            ) {
                DiskUsageCard(state.disks)
            }
        }

        state.media?.let { media ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(450, delayMillis = 420)) + slideInVertically(
                    tween(
                        450,
                        delayMillis = 420
                    )
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

    // Full-screen loading state
    if (state.isRefreshing && state.health == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = colorScheme.secondary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Connecting to agent…", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
            }
        }
    }



    state.screenshot?.let { bitmap ->
        ScreenshotSheet(
            bitmap = bitmap,
            onDismiss = { viewModel.dismissScreenshot() }
        )
    }
}

// ─── FAB Menu ────────────────────────────────────────────────────────────────

@Composable
fun DashboardFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onScreenshot: () -> Unit,
    onLock: () -> Unit,
    onPlayPause: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val gradient = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary))

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(220)) + expandVertically(
                tween(220),
                expandFrom = Alignment.Bottom
            ),
            exit = fadeOut(tween(180)) + shrinkVertically(
                tween(180),
                shrinkTowards = Alignment.Bottom
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrbitalFabItem(
                    onClick = onScreenshot,
                    icon = Icons.Default.PhotoCamera,
                    label = "Screenshot"
                )
                OrbitalFabItem(onClick = onLock, icon = Icons.Default.Lock, label = "Lock Screen")
                OrbitalFabItem(
                    onClick = onPlayPause,
                    icon = Icons.Default.PlayArrow,
                    label = "Play / Pause"
                )
            }
        }

        // Main FAB with gradient
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onToggle,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                modifier = Modifier.size(56.dp)
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(180))).togetherWith(
                            fadeOut(tween(120)) + scaleOut(tween(120))
                        )
                    },
                    label = "fab_icon"
                ) { expanded ->
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.GridView,
                        contentDescription = "Menu",
                        tint = colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitalFabItem(onClick: () -> Unit, icon: ImageVector, label: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            containerColor = colorScheme.secondaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(2.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Error Message ───────────────────────────────────────────────────────────

@Composable
fun ErrorMessage(msg: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.error.copy(alpha = 0.10f))
            .padding(14.dp)
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            null,
            tint = colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(msg, color = colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
