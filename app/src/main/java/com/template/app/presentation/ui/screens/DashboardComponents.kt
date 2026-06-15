package com.template.app.presentation.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.template.app.domain.model.*

@Composable
fun StatusCard(
    health: VelaHealth,
    isConnected: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) Color(0xFF10B981).copy(alpha = pulseAlpha)
                                else MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.error)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vela Agent", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Status: ${health.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = if (isRefreshing) Modifier.graphicsLayer { rotationZ += 12f } else Modifier
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SYSTEM UPTIME", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = formatUptime(health.uptimeSeconds),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkCard(network: VelaNetworkInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NETWORK ADAPTER", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Interface", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(network.interfaceName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Local IP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(network.localIp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text("Public IP Address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(network.publicIp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ProcessSummaryCard(processes: List<VelaProcess>, activeWindow: String?) {
    val totalCpu = processes.sumOf { it.cpu }
    val totalMem = processes.sumOf { it.mem }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("HOST RESOURCES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    ResourceMetric("CPU Summary", totalCpu, MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    ResourceMetric("RAM Summary", totalMem, MaterialTheme.colorScheme.secondary)
                }
            }
            if (processes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("TOP PROCESSES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                processes.take(5).forEach { process ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(process.name, modifier = Modifier.weight(1f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${process.cpu}%", modifier = Modifier.width(50.dp), textAlign = TextAlign.End, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("${process.mem}%", modifier = Modifier.width(50.dp), textAlign = TextAlign.End, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (!activeWindow.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ).padding(10.dp)
                ) {
                    Icon(Icons.Default.DesktopWindows, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("ACTIVE WINDOW", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(activeWindow, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceMetric(label: String, value: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${String.format("%.1f", value)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
    LinearProgressIndicator(
        progress = { (value / 100.0).coerceIn(0.0, 1.0).toFloat() },
        color = color,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(CircleShape)
    )
}

@Composable
fun AudioControlCard(
    audioState: VelaAudioState,
    onVolumeChange: (Int) -> Unit,
    onMuteToggle: (Boolean) -> Unit
) {
    var sliderValue by remember(audioState.volume) { mutableFloatStateOf(audioState.volume.toFloat()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AUDIO VOLUME", style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMuteToggle(!audioState.muted) }) {
                    Icon(
                        imageVector = if (audioState.muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (audioState.muted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onVolumeChange(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f)
                )
                Text("${sliderValue.toInt()}%", modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
fun BrightnessControlCard(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit
) {
    var sliderValue by remember(brightness) { mutableFloatStateOf(brightness.toFloat()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DISPLAY BRIGHTNESS", style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LightMode, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 12.dp).size(20.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onBrightnessChange(sliderValue.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f)
                )
                Text("${sliderValue.toInt()}%", modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
fun DiskUsageCard(disks: List<VelaDiskUsage>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dns, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISK STATUS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            disks.forEach { disk ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${disk.mountpoint}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", disk.percent)}% Used", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (disk.percent > 85.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { (disk.percent / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        color = if (disk.percent > 85.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(6.dp).clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaBar(
    media: VelaMediaState,
    onTogglePlayPause: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (media.status == "playing") Icons.Default.MusicNote else Icons.Default.Pause,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(media.title ?: "No Media Track", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(media.artist ?: "Unknown Artist", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (media.status == "playing") Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            if (media.positionSeconds != null && media.lengthSeconds != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (media.positionSeconds / media.lengthSeconds.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                )
            }
        }
    }
}

@Composable
fun ClipboardCard(
    currentText: String,
    onWriteText: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CLIPBOARD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text(currentText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Write to clipboard...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onWriteText(input); input = "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispatch to Host", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ScreenshotDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remote Display", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

fun formatUptime(totalSecs: Long): String {
    val hrs = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    val days = hrs / 24
    return if (days > 0) "${days}d ${hrs % 24}h ${mins}m ${secs}s" else "${hrs}h ${mins}m ${secs}s"
}
