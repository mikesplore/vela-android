package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.VelaBatteryStatus
import com.template.app.domain.model.VelaCpuUsage
import com.template.app.domain.model.VelaDiskIo
import com.template.app.domain.model.VelaFanInfo
import com.template.app.domain.model.VelaGpuUsage
import com.template.app.domain.model.VelaNetworkIo
import com.template.app.domain.model.VelaProcess
import com.template.app.domain.model.VelaRamUsage
import com.template.app.domain.model.VelaSensorInfo
import com.template.app.domain.model.VelaTemperatureInfo
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.ThinBar
import com.template.app.presentation.viewmodel.MonitorState
import com.template.app.presentation.viewmodel.MonitorViewModel
import java.util.Locale

@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MonitorContent(
            state = state,
            onIntervalChange = { viewModel.setUpdateInterval(it) }
        )
    }
}

@Composable
private fun MonitorContent(
    state: MonitorState,
    onIntervalChange: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            IntervalSelector(
                currentInterval = state.updateIntervalMs,
                onIntervalChange = onIntervalChange
            )
        }

        item {
            ProcessorSection(state.cpu, state.ram)
        }

        if (state.gpu.isNotEmpty()) {
            item {
                GpuSection(state.gpu)
            }
        }

        item {
            IoSection(state.diskIo, state.networkIo)
        }

        item {
            ThermalSection(state.temperatures, state.fans)
        }

        state.battery?.let {
            item {
                BatterySection(it)
            }
        }

        item {
            TopProcessesSection(state.topProcessesByCpu, "Top Processes (CPU)")
        }

        item {
            TopProcessesSection(state.topProcessesByMemory, "Top Processes (Memory)")
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IntervalSelector(
    currentInterval: Long,
    onIntervalChange: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Update interval",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IntervalPill(
                label = "1s",
                active = currentInterval == 1000L
            ) { onIntervalChange(1000L) }
            IntervalPill(
                label = "2s",
                active = currentInterval == 2000L
            ) { onIntervalChange(2000L) }
            IntervalPill(
                label = "5s",
                active = currentInterval == 5000L
            ) { onIntervalChange(5000L) }
        }
    }
}

@Composable
private fun IntervalPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.3f
        ),
        contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProcessorSection(cpu: VelaCpuUsage?, ram: VelaRamUsage?) {
    Column {
        SectionHeader("Processor")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GaugeBox(
                label = "CPU",
                value = "${cpu?.overall?.toInt() ?: 0}%",
                progress = (cpu?.overall?.toFloat() ?: 0f) / 100f,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            GaugeBox(
                label = "RAM",
                value = "${ram?.percent?.toInt() ?: 0}%",
                progress = (ram?.percent?.toFloat() ?: 0f) / 100f,
                color = MaterialTheme.colorScheme.secondary,
                subLabel = ram?.let { "${formatBytes(it.used)} / ${formatBytes(it.total)}" },
                modifier = Modifier.weight(1f)
            )
        }

        if (cpu != null && cpu.perCore.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            CoreGrid(cpu.perCore)
        }
    }
}

@Composable
private fun CoreGrid(perCore: List<Double>) {
    val columns = 4
    val rows = (perCore.size + columns - 1) / columns

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    if (index < perCore.size) {
                        CoreCell(
                            label = "C$index",
                            value = "${perCore[index].toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreCell(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun GpuSection(gpu: List<VelaGpuUsage>) {
    Column {
        SectionHeader("GPU")
        Spacer(Modifier.height(8.dp))
        gpu.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GaugeBox(
                    label = "Usage",
                    value = "${item.usagePercent.toInt()}%",
                    progress = (item.usagePercent.toFloat()) / 100f,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                GaugeBox(
                    label = "VRAM",
                    value = formatBytesShort(item.vramUsed),
                    progress = (item.vramPercent.toFloat()) / 100f,
                    color = MaterialTheme.colorScheme.tertiary,
                    subLabel = "${formatBytesShort(item.vramUsed)} / ${formatBytesShort(item.vramTotal)}",
                    modifier = Modifier.weight(1f)
                )
            }
            if (gpu.indexOf(item) < gpu.size - 1) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun IoSection(disks: List<VelaDiskIo>, networks: List<VelaNetworkIo>) {
    Column {
        SectionHeader("Disk & Network I/O")
        Spacer(Modifier.height(6.dp))

        if (disks.isNotEmpty()) {
            Text(
                text = "DISKS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            disks.forEach { disk ->
                IoRow(
                    icon = Icons.Rounded.Storage,
                    name = "Disk (${disk.device})",
                    up = disk.writeBytesPerSec,
                    down = disk.readBytesPerSec
                )
            }
        }

        if (disks.isNotEmpty() && networks.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
        }

        if (networks.isNotEmpty()) {
            Text(
                text = "NETWORKS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            networks.forEach { net ->
                IoRow(
                    icon = Icons.Rounded.NetworkCheck,
                    name = net.interfaceName,
                    up = net.bytesSentPerSec,
                    down = net.bytesRecvPerSec
                )
            }
        }
    }
}

@Composable
private fun IoRow(icon: ImageVector, name: String, up: Double, down: Double) {
    val isIdle = up == 0.0 && down == 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(if (isIdle) 0.35f else 1.0f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(6.dp)
                    .size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Fixed-width container layout guarantees right-aligned vertical columns
        Row(
            modifier = Modifier.width(170.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IoStat(
                icon = Icons.Rounded.CloudUpload,
                value = formatSpeed(up),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            IoStat(
                icon = Icons.Rounded.CloudDownload,
                value = formatSpeed(down),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IoStat(
    icon: ImageVector,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color.copy(alpha = 0.8f)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun ThermalSection(temperatures: List<VelaTemperatureInfo>, fans: List<VelaFanInfo>) {
    val coreTemps = temperatures.filter { it.sensor == "coretemp" }
    if (coreTemps.isEmpty()) return

    Column {
        SectionHeader("Thermal")
        Spacer(Modifier.height(8.dp))

        val columns = 4
        val rows = (coreTemps.size + columns - 1) / columns

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(rows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(columns) { c ->
                        val index = r * columns + c
                        if (index < coreTemps.size) {
                            val sensor = coreTemps[index]
                            SensorBox(
                                label = sensor.label,
                                value = "${sensor.current.toInt()}°C",
                                statusColor = when {
                                    sensor.critical != null && sensor.current >= sensor.critical -> MaterialTheme.colorScheme.error
                                    sensor.high != null && sensor.current >= sensor.high -> Color(0xFFE8A440)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorBox(
    label: String,
    value: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp),

    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = statusColor
            )
        }
    }
}

@Composable
private fun BatterySection(battery: VelaBatteryStatus) {
    Column {
        SectionHeader("Battery")
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (battery.pluggedIn) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (battery.pluggedIn) "Plugged in" else "On battery",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${battery.percent.toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = if (battery.percent < 20 && !battery.pluggedIn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        ThinBar(
            progress = (battery.percent / 100f).toFloat(),
            color = when {
                battery.percent < 20 && !battery.pluggedIn -> MaterialTheme.colorScheme.error
                battery.pluggedIn -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )
    }
}

@Composable
private fun TopProcessesSection(processes: List<VelaProcess>, title: String) {
    Column {
        SectionHeader(title)
        Spacer(Modifier.height(8.dp))
        Column {
            processes.forEach { proc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        proc.name,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${proc.cpu.toInt()}%",
                        modifier = Modifier.width(56.dp),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${proc.mem.toInt()}%",
                        modifier = Modifier.width(56.dp),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (processes.indexOf(proc) < processes.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }
            }
        }
    }
}


@Composable
private fun GaugeBox(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    subLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(Modifier.height(8.dp))
            ThinBar(progress = progress, color = color)

            Text(
                subLabel ?: "",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroup = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        Locale.ROOT,
        "%.1f %s",
        bytes / Math.pow(1024.0, digitGroup.toDouble()),
        units[digitGroup]
    )
}

private fun formatBytesShort(bytes: Long): String {
    if (bytes <= 0) return "0"
    if (bytes < 1024 * 1024 * 1024) return "${bytes / (1024 * 1024)}M"
    return String.format(Locale.ROOT, "%.1fG", bytes / (1024.0 * 1024.0 * 1024.0))
}

private fun formatSpeed(bytesPerSec: Double): String {
    if (bytesPerSec < 1024) return "${bytesPerSec.toInt()} B/s"
    if (bytesPerSec < 1024 * 1024) return "${(bytesPerSec / 1024).toInt()} KB/s"
    return String.format(Locale.ROOT, "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
}
