package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.DockerContainer
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.DockerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerScreen(
    viewModel: DockerViewModel = hiltViewModel()
) {
    val info by viewModel.info.collectAsStateWithLifecycle()
    val containers by viewModel.containers.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        if (ui.selectedId != null) {
            DockerDetailPane(
                ui = ui,
                onBack = viewModel::clearSelection,
                onStart = viewModel::startSelected,
                onStop = viewModel::stopSelected,
                onRestart = viewModel::restartSelected
            )
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Daemon")
            Spacer(modifier = Modifier.height(8.dp))

            val running = info?.running == true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (running) Color(0xFF22C55E) else colorScheme.error)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            info == null -> "Loading…"
                            info?.installed != true -> "Docker not installed"
                            running -> "Daemon running"
                            else -> "Daemon stopped"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = buildString {
                            info?.version?.let { append("v$it · ") }
                            append("${info?.containersRunning ?: 0} running / ${info?.containersTotal ?: 0} total")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    info?.message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Containers")
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ui.filter,
                onValueChange = viewModel::setFilter,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Filter") },
                trailingIcon = {
                    TextButton(onClick = viewModel::refreshAll) { Text("Apply") }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(containers, key = { it.id }) { container ->
                    DockerContainerRow(container) { viewModel.selectContainer(container.id) }
                }
                if (containers.isEmpty() && !ui.isRefreshing) {
                    item {
                        Text(
                            "No containers found",
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(title = "Compose")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ui.composeProject,
                onValueChange = viewModel::setComposeProject,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Project name (optional)") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ui.composeDirectory,
                onValueChange = viewModel::setComposeDirectory,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Project directory (optional)") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = viewModel::loadCompose, modifier = Modifier.fillMaxWidth()) {
                Text("Load compose status")
            }
            ui.compose?.let { compose ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Project: ${compose.project ?: "default"}",
                    fontWeight = FontWeight.Medium
                )
                compose.services.forEach { svc ->
                    Text(
                        "${svc.name} · ${svc.state} · ${svc.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DockerContainerRow(container: DockerContainer, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val running = container.state.equals("running", ignoreCase = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Storage,
            contentDescription = null,
            tint = if (running) Color(0xFF22C55E) else colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(container.name.ifBlank { container.id }, fontWeight = FontWeight.SemiBold)
            Text(
                container.image,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                container.status,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilterChip(
            selected = running,
            onClick = onClick,
            label = { Text(container.state.ifBlank { "unknown" }, fontSize = 11.sp) }
        )
    }
}

@Composable
private fun DockerDetailPane(
    ui: com.template.app.presentation.viewmodel.DockerUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    val detail = ui.detail
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = detail?.name ?: ui.selectedId.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (detail == null) {
            CircularProgressIndicator()
        } else {
            Text("Image: ${detail.image}")
            Text("State: ${detail.state}")
            Text("Status: ${detail.status}")
            detail.health?.let { Text("Health: $it") }
            if (detail.ports.isNotEmpty()) {
                Text("Ports: ${detail.ports.joinToString()}")
            }
            detail.startedAt?.let { Text("Started: $it") }
            detail.finishedAt?.let { Text("Finished: $it") }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStart, enabled = !ui.actionBusy) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Start")
                }
                Button(onClick = onStop, enabled = !ui.actionBusy) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Stop")
                }
                Button(onClick = onRestart, enabled = !ui.actionBusy) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restart")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader(title = "Logs")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = ui.logs?.lines?.joinToString("\n").orEmpty().ifBlank { "No logs" },
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceVariant)
                .padding(12.dp)
        )
    }
}
