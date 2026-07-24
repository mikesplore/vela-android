package com.template.app.presentation.ui.screens.docker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.app.presentation.ui.components.ActionButton
import com.template.app.presentation.ui.components.DataRow
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.rowDivider
import com.template.app.presentation.ui.theme.DarkSuccess
import com.template.app.presentation.viewmodel.DockerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerDetailPane(
    ui: DockerUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    val detail = ui.detail
    val colorScheme = MaterialTheme.colorScheme
    var showSheet by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Compact Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail?.name?.removePrefix("/") ?: ui.selectedId?.take(12)
                        ?: "Container",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    detail?.image?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (detail != null) {
                    val stateStr = detail.state.lowercase()
                    val isRunning = stateStr == "running"
                    val isTransitioning = stateStr in listOf("starting", "stopping", "restarting")

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isRunning -> DarkSuccess
                                    isTransitioning -> colorScheme.primary
                                    else -> colorScheme.error
                                }
                            )
                    )
                }

                IconButton(onClick = { showSheet = true }) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Actions & Details",
                        modifier = Modifier.size(20.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            if (detail == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                }
            } else {
                // 2. Full-Screen Log Terminal (Main Hero)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TERMINAL LOGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Last 100 lines",
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    val logScrollState = rememberScrollState()
                    LaunchedEffect(ui.logs) {
                        logScrollState.animateScrollTo(logScrollState.maxValue)
                    }

                    Text(
                        text = ui.logs?.lines?.joinToString("\n").orEmpty()
                            .ifBlank { "No logs available" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(logScrollState)
                            .padding(16.dp)
                    )
                }
            }
        }

        // 3. Modal Bottom Sheet
        if (showSheet && detail != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                dragHandle = { BottomSheetDefaults.DragHandle() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                DockerControlSheet(
                    detail = detail,
                    ui = ui,
                    onStart = onStart,
                    onStop = onStop,
                    onRestart = onRestart
                )
            }
        }
    }
}

@Composable
private fun DockerControlSheet(
    detail: com.template.app.domain.model.DockerContainerDetail,
    ui: DockerUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val stateStr = detail.state.lowercase()
    val isRunning = stateStr == "running"
    val isTransitioning = stateStr in listOf("starting", "stopping", "restarting")
    val showStopRestart = isRunning || isTransitioning

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.name.removePrefix("/"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = detail.image,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Box(Modifier.then(rowDivider(colorScheme)))
        Spacer(Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!showStopRestart) {
                ActionButton(
                    icon = Icons.Default.PlayArrow,
                    label = "START",
                    color = DarkSuccess,
                    onClick = onStart,
                    busy = ui.actionBusy,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ActionButton(
                    icon = Icons.Default.Stop,
                    label = "STOP",
                    color = colorScheme.error,
                    onClick = onStop,
                    busy = ui.actionBusy || !isRunning,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Default.Refresh,
                    label = "RESTART",
                    color = colorScheme.secondary,
                    onClick = onRestart,
                    busy = ui.actionBusy || !isRunning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        SectionHeader("Overview")
        Spacer(Modifier.height(12.dp))
        DataRow(
            "Current State",
            detail.state.uppercase(),
            valueColor = if (isRunning) DarkSuccess else colorScheme.error
        )
        Box(Modifier.then(rowDivider(colorScheme)))
        DataRow("Status String", detail.status)
        detail.health?.let {
            Box(Modifier.then(rowDivider(colorScheme)))
            DataRow(
                "Health",
                it,
                valueColor = if (it == "healthy") DarkSuccess else colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader("Networking")
        Spacer(Modifier.height(12.dp))
        if (detail.ports.isEmpty()) {
            Text(
                "No ports exposed",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        } else {
            detail.ports.forEachIndexed { index, port ->
                DataRow("Port Mapping", port)
                if (index < detail.ports.lastIndex) Box(Modifier.then(rowDivider(colorScheme)))
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader("Timeline")
        Spacer(Modifier.height(12.dp))
        detail.startedAt?.let { DataRow("Started At", it.take(19).replace("T", " ")) }
        detail.finishedAt?.takeIf { it != "0001-01-01T00:00:00Z" }?.let {
            Box(Modifier.then(rowDivider(colorScheme)))
            DataRow("Finished At", it.take(19).replace("T", " "))
        }
    }
}
