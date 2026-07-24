package com.template.app.presentation.ui.screens.docker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.DockerInfo
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
    var showComposeSheet by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Minimalist Daemon Info Bar
            DaemonStatusBar(info = info)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Containers")
                IconButton(onClick = { showComposeSheet = true }) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Compose",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search/Filter bar
            DockerSearchBar(
                value = ui.filter,
                onValueChange = viewModel::setFilter,
                onApply = viewModel::refreshAll
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(containers, key = { it.id }) { container ->
                    DockerContainerRow(container) { viewModel.selectContainer(container.id) }
                }
                if (containers.isEmpty() && !ui.isRefreshing) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No containers found",
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        if (showComposeSheet) {
            DockerComposeSheet(
                ui = ui,
                onProjectChange = viewModel::setComposeProject,
                onDirectoryChange = viewModel::setComposeDirectory,
                onLoad = viewModel::loadCompose,
                onDismiss = { showComposeSheet = false }
            )
        }
    }
}

@Composable
private fun DaemonStatusBar(info: DockerInfo?) {
    val colorScheme = MaterialTheme.colorScheme
    val running = info?.running == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (running) Color(0xFF22C55E) else colorScheme.error)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (running) "Docker Engine Active" else "Docker Engine Inactive",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        info?.version?.let {
            Text(
                text = "v$it",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = colorScheme.primary.copy(alpha = 0.7f)
            )
        }
        if (running) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${info.containersRunning ?: 0} running",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DockerSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search containers...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                TextButton(onClick = onApply) {
                    Text("Refresh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),

        textStyle = MaterialTheme.typography.bodyMedium
    )
}
