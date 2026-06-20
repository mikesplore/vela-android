package com.template.app.presentation.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.template.app.domain.model.*
import com.template.app.presentation.ui.components.DataRow
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.ThinBar
import com.template.app.presentation.ui.components.rowDivider
import com.template.app.presentation.ui.theme.DarkSuccess
import java.util.Locale

// ─── Design tokens ────────────────────────────────────────────────────────────

private val CardPadding     = 20.dp
private val SectionSpacing  = 20.dp

private fun diskColor(pct: Double, cs: ColorScheme): Color = when {
    pct > 85 -> cs.error
    pct > 60 -> cs.tertiary
    else     -> cs.primary
}
private fun signalColor(signal: Int, cs: ColorScheme): Color = when {
    signal > 70 -> DarkSuccess
    signal > 40 -> cs.tertiary
    else        -> cs.error
}

// ─── Base card ────────────────────────────────────────────────────────────────
//
// VelaCard is now transparent to match the screen's background gradient, making
// the dashboard feel lighter and more integrated.

@Composable
private fun VelaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(CardPadding),
        content = content
    )
}

// ─── StatusCard ───────────────────────────────────────────────────────────────

@Composable
fun StatusCard(
    health: VelaHealth
) {
    VelaCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader("System uptime")
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatUptime(health.uptimeSeconds),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 52.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── NetworkCard ──────────────────────────────────────────────────────────────

@Composable
fun NetworkCard(network: VelaNetworkInfo, wifi: VelaWifiStatus?) {
    val cs = MaterialTheme.colorScheme

    VelaCard {
        SectionHeader("Network")
        Spacer(Modifier.height(SectionSpacing))

        DataRow("SSID", wifi?.ssid ?: "—")
        Box(Modifier.then(rowDivider(cs)))
        wifi?.signal?.let { sig ->
            DataRow("Signal", "$sig%", valueColor = signalColor(sig, cs))
            Box(Modifier.then(rowDivider(cs)))
        }

        DataRow("Local IP", network.localIp)
        Box(Modifier.then(rowDivider(cs)))
        DataRow("Public IP", network.publicIp?:"-", valueColor = cs.onSurfaceVariant)

        wifi?.signal?.let { sig ->
            Spacer(Modifier.height(12.dp))
            ThinBar(progress = sig / 100f, color = signalColor(sig, cs))
        }
    }
}

// ─── SystemResolutionCard ─────────────────────────────────────────────────────

@Composable
fun SystemResolutionCard(resolution: VelaResolution?) {
    if (resolution == null) return
    val cs = MaterialTheme.colorScheme

    VelaCard {
        SectionHeader("Display")
        Spacer(Modifier.height(SectionSpacing))
        DataRow("Resolution", "${resolution.width}×${resolution.height}")
        Box(Modifier.then(rowDivider(cs)))
        DataRow("Refresh rate", "${resolution.refresh} Hz", valueColor = cs.primary)
        resolution.output?.let { output ->
            Box(Modifier.then(rowDivider(cs)))
            DataRow("Output", output)
        }
    }
}

// ─── ResourceGauge (private) ──────────────────────────────────────────────────

@Composable
private fun ResourceGauge(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Transparent)
            .padding(12.dp)
    ) {
        Text(
            label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${String.format("%.1f", value)}%",
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        ThinBar(progress = (value / 100.0).toFloat(), color = color)
    }
}

// ─── ProcessRow (private) ─────────────────────────────────────────────────────

@Composable
private fun ProcessRow(process: VelaProcess) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = process.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = cs.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = String.format(Locale.ROOT, "%.2f%%", process.cpu),
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            color = cs.primary,
            maxLines = 1
        )
        Text(
            text = String.format(Locale.ROOT, "%.2f%%", process.mem),
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            color = cs.secondary,
            maxLines = 1
        )
    }
}

// ─── ProcessSummaryCard ───────────────────────────────────────────────────────

@Composable
fun ProcessSummaryCard(
    processes: List<VelaProcess>,
    activeWindow: String?,
    currentLimit: Int,
    cpuUsage: Double,
    ramUsage: Double,
    onToggleLimit: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    VelaCard {
        SectionHeader("Resources")
        Spacer(Modifier.height(SectionSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResourceGauge("CPU", cpuUsage, cs.primary, Modifier.weight(1f))
            ResourceGauge("RAM", ramUsage, cs.secondary, Modifier.weight(1f))
        }

        Spacer(Modifier.height(SectionSpacing))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Process",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = cs.onSurfaceVariant.copy(alpha = 0.5f),
                letterSpacing = 0.4.sp
            )
            Text(
                "CPU",
                modifier = Modifier.width(68.dp),
                textAlign = TextAlign.End,
                fontSize = 13.sp,
                color = cs.primary.copy(alpha = 0.6f),
                letterSpacing = 0.4.sp
            )
            Text(
                "MEM",
                modifier = Modifier.width(68.dp),
                textAlign = TextAlign.End,
                fontSize = 13.sp,
                color = cs.secondary.copy(alpha = 0.6f),
                letterSpacing = 0.4.sp
            )
        }

        Box(Modifier.then(rowDivider(cs)))
        Spacer(Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            if (processes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No processes found",
                        fontSize = 12.sp,
                        color = cs.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    processes.forEach { ProcessRow(it) }
                }
            }
        }

        TextButton(
            onClick = onToggleLimit,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (currentLimit == 5) "Show more" else "Show less",
                fontSize = 11.sp,
                color = cs.secondary
            )
        }
    }
}

// ─── AudioControlCard ─────────────────────────────────────────────────────────

@Composable
fun AudioControlCard(
    audioState: VelaAudioState,
    onVolumeChange: (Int) -> Unit,
    onMuteToggle: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var sliderValue by remember(audioState.volume) { mutableFloatStateOf(audioState.volume.toFloat()) }
    val isMuted = audioState.muted

    VelaCard {
        SectionHeader("Audio")
        Spacer(Modifier.height(SectionSpacing))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onMuteToggle(!isMuted) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isMuted)
                        Icons.AutoMirrored.Filled.VolumeOff
                    else
                        Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = cs.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onVolumeChange(sliderValue.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = cs.primary,
                    activeTrackColor = cs.primary,
                    inactiveTrackColor = cs.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${sliderValue.toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─── BrightnessControlCard ──────────────────────────────────────────────────

@Composable
fun BrightnessControlCard(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var sliderValue by remember(brightness) { mutableFloatStateOf(brightness.toFloat()) }

    VelaCard {
        SectionHeader("Brightness")
        Spacer(Modifier.height(SectionSpacing))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onBrightnessChange(sliderValue.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = cs.tertiary,
                    activeTrackColor = cs.tertiary,
                    inactiveTrackColor = cs.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${sliderValue.toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─── DiskUsageCard ────────────────────────────────────────────────────────────

@Composable
fun DiskUsageCard(disks: List<VelaDiskUsage>) {
    val cs = MaterialTheme.colorScheme

    VelaCard {
        SectionHeader("Storage")
        Spacer(Modifier.height(SectionSpacing))

        disks.forEachIndexed { index, disk ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = disk.mountpoint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onSurface
                        )
                        Text(
                            text = "${disk.used} / ${disk.total}",
                            fontSize = 11.sp,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", disk.percent)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = diskColor(disk.percent, cs)
                    )
                }
                Spacer(Modifier.height(8.dp))
                ThinBar(
                    progress = (disk.percent / 100.0).toFloat(),
                    color = diskColor(disk.percent, cs)
                )

                if (index < disks.size - 1) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─── MediaBar ─────────────────────────────────────────────────────────────────

@Composable
fun MediaBar(
    media: VelaMediaState,
    onTogglePlayPause: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    VelaCard {
        SectionHeader("Current Song")
        Spacer(Modifier.height(SectionSpacing))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cs.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!media.artUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = media.artUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                media.title?.ifBlank { "Nothing playing" }?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = media.artist?.ifBlank { "Nothing playing" } ?: "-",
                    fontSize = 11.sp,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlayPause) {
                Icon(
                    imageVector = if (media.status == "Playing") Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = cs.primary
                )
            }
        }
    }
}



// ─── ScreenshotSheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotSheet(
    bitmap: Bitmap?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp) // Ensure it has height even when empty
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Capturing Screenshot...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "System Screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                // This covers the edge case where loading finished but bitmap is still null (error)
                Text("No image available", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatUptime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.ROOT, "%02d:%02d:%02d", hrs, mins, secs)
}
