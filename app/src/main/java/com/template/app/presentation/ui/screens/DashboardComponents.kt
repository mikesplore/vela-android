package com.template.app.presentation.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.template.app.domain.model.*

// ─── Base Card Shell ─────────────────────────────────────────────────────────

@Composable
private fun OrbitalCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.35f), colorScheme.outlineVariant)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            // Left accent glow strip
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.8f), Color.Transparent)
                    ),
                    topLeft = Offset(0f, size.height * 0.15f),
                    size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height * 0.7f)
                )
            }
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

// ─── Section Label ───────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(
    text: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.secondary
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Stat Chip ───────────────────────────────────────────────────────────────

@Composable
private fun StatBlock(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = valueColor)
    }
}

// ─── Gradient Progress Bar ───────────────────────────────────────────────────

@Composable
private fun GradientProgress(
    progress: Float,
    brush: Brush? = null,
    color: Color? = null,
    height: Dp = 5.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val colorScheme = MaterialTheme.colorScheme
    val finalBrush = brush ?: color?.let { SolidColor(it) } ?: Brush.horizontalGradient(
        listOf(colorScheme.primary, colorScheme.secondary)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(finalBrush)
        )
    }
}

// ─── StatusCard ──────────────────────────────────────────────────────────────

@Composable
fun StatusCard(
    health: VelaHealth,
    isConnected: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val connectedColor = if (isConnected) Color(0xFF4CAF50) else colorScheme.error

    OrbitalCard(accentColor = connectedColor) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, colorScheme.outlineVariant, Color.Transparent)
                    )
                )
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text("SYSTEM UPTIME", fontSize = 10.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 1.6.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatUptime(health.uptimeSeconds),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            fontFamily = FontFamily.Monospace,
            color = colorScheme.onSurface
        )
    }
}

// ─── NetworkCard ─────────────────────────────────────────────────────────────

@Composable
fun NetworkCard(network: VelaNetworkInfo, wifi: VelaWifiStatus?) {
    val colorScheme = MaterialTheme.colorScheme
    val signalColor = when {
        (wifi?.signal ?: 0) > 70 -> Color(0xFF4CAF50)
        (wifi?.signal ?: 0) > 40 -> Color(0xFFFFB300)
        else -> colorScheme.error
    }

    OrbitalCard(accentColor = colorScheme.secondary) {
        SectionLabel("NETWORK & WIFI", Icons.Default.Wifi, colorScheme.secondary)
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatBlock("Interface", network.interfaceName.ifBlank { "—" })
            StatBlock("Local IP", network.localIp, colorScheme.secondary)
        }

        if (wifi != null && wifi.connected) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatBlock("SSID", wifi.ssid ?: "Unknown")
                wifi.signal?.let { signal ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Signal", fontSize = 10.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$signal%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = signalColor)
                    }
                }
            }
            wifi.signal?.let { signal ->
                Spacer(modifier = Modifier.height(8.dp))
                GradientProgress(
                    progress = signal / 100f,
                    color = signalColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.secondary.copy(alpha = 0.06f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text("Public IP", fontSize = 10.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    network.publicIp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    color = colorScheme.secondary
                )
            }
        }
    }
}

// ─── SystemResolutionCard ────────────────────────────────────────────────────

@Composable
fun SystemResolutionCard(resolution: VelaResolution?) {
    if (resolution == null) return
    val colorScheme = MaterialTheme.colorScheme

    OrbitalCard(accentColor = colorScheme.primary) {
        SectionLabel("SYSTEM DISPLAY", Icons.Default.Monitor, colorScheme.primary)
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatBlock("Resolution", "${resolution.width}×${resolution.height}")
            StatBlock("Refresh Rate", "${resolution.refresh} Hz", colorScheme.primary)
        }
        resolution.output?.let { output ->
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Output: $output", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── ProcessSummaryCard ──────────────────────────────────────────────────────

@Composable
fun ProcessSummaryCard(
    processes: List<VelaProcess>,
    activeWindow: String?,
    currentLimit: Int,
    onToggleLimit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalCpu = processes.sumOf { it.cpu }
    val totalMem = processes.sumOf { it.mem }

    OrbitalCard(accentColor = colorScheme.tertiary) {
        SectionLabel("HOST RESOURCES", Icons.Default.Memory, colorScheme.tertiary)
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResourceGauge(
                label = "CPU",
                value = totalCpu,
                color = colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            ResourceGauge(
                label = "RAM",
                value = totalMem,
                color = colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOP PROCESSES", fontSize = 10.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 1.4.sp)
            TextButton(
                onClick = onToggleLimit,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (currentLimit == 5) "Show More" else "Show Less",
                    fontSize = 11.sp,
                    color = colorScheme.secondary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            if (processes.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text("No processes found", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    processes.forEach { process ->
                        ProcessRow(process)
                    }
                }
            }
        }

        if (!activeWindow.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.tertiary.copy(alpha = 0.07f))
                    .border(1.dp, colorScheme.tertiary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.DesktopWindows, null, tint = colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("ACTIVE WINDOW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(activeWindow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun ResourceGauge(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.06f))
            .padding(14.dp)
    ) {
        Text(label, fontSize = 10.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "${String.format("%.1f", value)}%",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        GradientProgress(
            progress = (value / 100.0).toFloat(),
            color = color,
            height = 4.dp
        )
    }
}

@Composable
private fun ProcessRow(process: VelaProcess) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colorScheme.primary.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            process.name,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${process.cpu}%",
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
            fontSize = 11.sp,
            color = colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "${process.mem}%",
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
            fontSize = 11.sp,
            color = colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── AudioControlCard ────────────────────────────────────────────────────────

@Composable
fun AudioControlCard(
    audioState: VelaAudioState,
    onVolumeChange: (Int) -> Unit,
    onMuteToggle: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var sliderValue by remember(audioState.volume) { mutableFloatStateOf(audioState.volume.toFloat()) }
    val isMuted = audioState.muted
    val accentColor = if (isMuted) colorScheme.error else Color(0xFF4CAF50)

    OrbitalCard(accentColor = accentColor) {
        SectionLabel("AUDIO VOLUME", Icons.Default.VolumeUp, accentColor)
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { onMuteToggle(!isMuted) }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onVolumeChange(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${sliderValue.toInt()}%",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isMuted) colorScheme.error else colorScheme.onSurface
            )
        }
    }
}

// ─── BrightnessControlCard ───────────────────────────────────────────────────

@Composable
fun BrightnessControlCard(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = Color(0xFFFFB300)
    var sliderValue by remember(brightness) { mutableFloatStateOf(brightness.toFloat()) }

    OrbitalCard(accentColor = accentColor) {
        SectionLabel("DISPLAY BRIGHTNESS", Icons.Default.LightMode, accentColor)
        Spacer(modifier = Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LightMode, null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onBrightnessChange(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${sliderValue.toInt()}%",
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accentColor
            )
        }
    }
}

// ─── DiskUsageCard ───────────────────────────────────────────────────────────

@Composable
fun DiskUsageCard(disks: List<VelaDiskUsage>) {
    val colorScheme = MaterialTheme.colorScheme
    OrbitalCard(accentColor = colorScheme.primary) {
        SectionLabel("DISK STATUS", Icons.Default.Dns, colorScheme.primary)
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            disks.forEach { disk ->
                val usedPercent = disk.percent
                val diskColor = when {
                    usedPercent > 85 -> colorScheme.error
                    usedPercent > 60 -> Color(0xFFFFB300)
                    else -> colorScheme.primary
                }
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            disk.mountpoint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "${String.format("%.1f", usedPercent)}% used",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = diskColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GradientProgress(
                        progress = (usedPercent / 100.0).toFloat(),
                        color = diskColor,
                        height = 6.dp
                    )
                }
            }
        }
    }
}

// ─── MediaBar ────────────────────────────────────────────────────────────────

@Composable
fun MediaBar(
    media: VelaMediaState,
    onTogglePlayPause: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isPlaying = media.status == "playing"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surfaceContainer)
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(colorScheme.primary.copy(0.4f), colorScheme.secondary.copy(0.4f))),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Album art placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(colorScheme.primary.copy(0.3f), colorScheme.secondary.copy(0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = null,
                        tint = if (isPlaying) colorScheme.secondary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        media.title ?: "No Media Track",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        media.artist ?: "Unknown Artist",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(colorScheme.secondary.copy(0.25f), Color.Transparent)
                            )
                        )
                        .border(1.dp, colorScheme.secondary.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            if (media.positionSeconds != null && media.lengthSeconds != null) {
                Spacer(modifier = Modifier.height(14.dp))
                val progress = (media.positionSeconds / media.lengthSeconds.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                GradientProgress(progress = progress, height = 3.dp, trackColor = colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(media.positionSeconds.toLong()), fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                    Text(formatDuration(media.lengthSeconds.toLong()), fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun formatDuration(secs: Long): String {
    val m = secs / 60; val s = secs % 60
    return "${m}:${s.toString().padStart(2, '0')}"
}

// ─── ClipboardCard ───────────────────────────────────────────────────────────

@Composable
fun ClipboardCard(
    currentText: String,
    onWriteText: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var input by remember { mutableStateOf("") }

    OrbitalCard(accentColor = colorScheme.secondary) {
        SectionLabel("CLIPBOARD", Icons.Default.ContentPaste, colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))

        // Current clipboard content preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            if (currentText.isBlank()) {
                Text("Clipboard is empty", fontSize = 12.sp, color = colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                Text(
                    currentText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text("Write to clipboard...", fontSize = 13.sp, color = colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.secondary,
                unfocusedBorderColor = colorScheme.outlineVariant,
                cursorColor = colorScheme.secondary,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface
            ),
            minLines = 2
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onWriteText(input); input = "" },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispatch to Host", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colorScheme.onPrimary)
            }
        }
    }
}

// ─── ScreenshotSheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotSheet(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Current Screenshot",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Current Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            ) {
                Text("Close", fontWeight = FontWeight.Bold, color = colorScheme.onPrimary)
            }
        }
    }
}

// ─── Utility ─────────────────────────────────────────────────────────────────

fun formatUptime(totalSecs: Long): String {
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    val days = hrs / 24
    return if (days > 0) "${days}d ${hrs % 24}h ${mins}m ${secs}s" else "${hrs}h ${mins}m ${secs}s"
}
