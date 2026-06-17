package com.template.app.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.template.app.domain.model.VelaAudioDevice
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.AudioViewModel
import kotlin.math.atan2

@Composable
fun AudioScreen(
    viewModel: AudioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Create scroll state for the whole screen
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Make the entire column scrollable
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Volume Ring Zone
            VolumeRingZone(
                volume = uiState.audioState?.volume ?: 0,
                modifier = Modifier.padding(top = 36.dp, bottom = 16.dp),
                onVolumeChange = { viewModel.setVolume(it) }
            )

            // Step Controls
            StepRow(
                isMuted = uiState.audioState?.muted ?: false,
                onVolumeDown = { viewModel.volumeDown() },
                onVolumeUp = { viewModel.volumeUp() },
                onToggleMute = { viewModel.toggleMute() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Microphone Control
            SectionHeader("Microphone")
            Spacer(modifier = Modifier.height(10.dp))

            MicControlSection(
                isMicMuted = uiState.audioState?.micMuted ?: false,
                onToggleMicMute = { viewModel.toggleMicMute() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Device Selection Section
            SectionHeader("Output device")
            Spacer(modifier = Modifier.height(10.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp), // Added extra bottom padding for better scroll clearance
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.devices.forEach { device ->
                    DeviceItem(
                        device = device,
                        isSelected = device.isActive,
                        onClick = { viewModel.selectDevice(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeRingZone(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()

                        fun handleTouch(pos: androidx.compose.ui.geometry.Offset) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val angleRad = atan2(pos.y - centerY, pos.x - centerX)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                            if (angleDeg < 0) angleDeg += 360f
                            val newVolume = (angleDeg / 360f * 100).coerceIn(0f, 100f).toInt()
                            onVolumeChange(newVolume)
                        }

                        handleTouch(down.position)

                        drag(down.id) { change ->
                            change.consume()
                            handleTouch(change.position)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = size.minDimension / 2

                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth)
                )

                val sweepAngle = (volume / 100f) * 360f
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val angleInRad = (sweepAngle - 90f) * (Math.PI / 180f).toFloat()
                val thumbX = center.x + radius * kotlin.math.cos(angleInRad)
                val thumbY = center.y + radius * kotlin.math.sin(angleInRad)

                drawCircle(
                    color = primaryColor,
                    radius = 12.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(thumbX, thumbY)
                )
                drawCircle(
                    color = onPrimaryColor,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(thumbX, thumbY)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$volume%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "SWIPE RADIALLY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StepRow(
    isMuted: Boolean,
    onVolumeDown: () -> Unit,
    onVolumeUp: () -> Unit,
    onToggleMute: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onVolumeDown,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Down")
        }

        Spacer(modifier = Modifier.width(24.dp))

        Button(
            onClick = onToggleMute,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isMuted) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Mute",
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        FilledTonalIconButton(
            onClick = onVolumeUp,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Up")
        }
    }
}

@Composable
private fun MicControlSection(
    isMicMuted: Boolean,
    onToggleMicMute: () -> Unit
) {
    Column {
        Surface(
            onClick = onToggleMicMute,
            shape = RoundedCornerShape(16.dp),
            // Use surfaceContainer for a subtle card look, or errorContainer if muted
            color = if (isMicMuted) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer // Correct M3 token for adaptive cards
            },
            // Add a subtle border when not muted to match the app's clean design pattern
            border = if (!isMicMuted) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            } else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Background icon container to match the DeviceItem pattern
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isMicMuted) {
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isMicMuted) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMicMuted) "Microphone Muted" else "Microphone Active",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isMicMuted) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = if (isMicMuted) "Security: Mic is disabled" else "Security: Mic is enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switch uses standard M3 colors which handle light/dark automatically
                Switch(
                    checked = !isMicMuted,
                    onCheckedChange = { onToggleMicMute() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
            }
        }
    }
}


@Composable
private fun DeviceItem(
    device: VelaAudioDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getDeviceIcon(device.type),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${device.type} • ${if (isSelected) "Active" else "Available"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = null // Handled by Surface click
            )
        }
    }
}

private fun getDeviceIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "headphones", "bluetooth" -> Icons.Default.Headphones
        "speaker", "usb" -> Icons.Default.Speaker
        "hdmi", "tv" -> Icons.Default.Tv
        else -> Icons.Default.Devices
    }
}
