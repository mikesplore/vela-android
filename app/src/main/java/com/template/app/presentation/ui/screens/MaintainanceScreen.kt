package com.template.app.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.VelaPackageUpdate
import com.template.app.domain.model.VelaService
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.SecureConfirmGate
import com.template.app.presentation.ui.theme.DarkSuccess
import com.template.app.presentation.ui.theme.DarkWarning
import com.template.app.presentation.ui.theme.LightSuccess
import com.template.app.presentation.ui.theme.LightWarning
import com.template.app.presentation.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onBack: () -> Unit = {},
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUpdateConfirm by remember { mutableStateOf(false) }
    val isDark = !MaterialTheme.colorScheme.isLight()
    val successColor = if (isDark) DarkSuccess else LightSuccess
    val warningColor = if (isDark) DarkWarning else LightWarning

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (uiState.isLoading && uiState.visibleServices.isEmpty() && uiState.totalServiceCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        } else {
            if (uiState.availableUpdates.isNotEmpty()) {
                UpdateCard(
                    manager = uiState.updateManager,
                    packages = uiState.availableUpdates,
                    isUpdating = uiState.isUpdating,
                    warningColor = warningColor,
                    onRunUpdate = { showUpdateConfirm = true }
                )
            }

            MaintenanceSection(title = "Services") {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Search all cached services…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearch("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        uiState.totalServiceCount == 0 -> "No services cached yet"
                        uiState.searchQuery.isBlank() ->
                            "Showing ${uiState.visibleServices.size} of ${uiState.matchedCount}"
                        else ->
                            "Showing ${uiState.visibleServices.size} of ${uiState.matchedCount} matches · ${uiState.totalServiceCount} cached"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.visibleServices.isEmpty()) {
                    EmptyHint(
                        if (uiState.searchQuery.isBlank()) "No services loaded yet"
                        else "No services match “${uiState.searchQuery}”"
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.visibleServices.forEach { service ->
                            val expanded = uiState.expandedService == service.name
                            ServiceCard(
                                service = service,
                                expanded = expanded,
                                logs = if (expanded) uiState.serviceLogs else emptyList(),
                                isLoadingLogs = expanded && uiState.isLoadingLogs,
                                successColor = successColor,
                                onToggle = { viewModel.toggleServiceExpanded(service.name) },
                                onRefreshLogs = { viewModel.refreshLogs() },
                                onStart = { viewModel.startService(service.name) },
                                onStop = { viewModel.stopService(service.name) },
                                onRestart = { viewModel.restartService(service.name) }
                            )
                        }

                        if (uiState.canLoadMore) {
                            OutlinedButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Load more")
                            }
                        }
                    }
                }
            }

            HostUpkeepBar(
                onClearCache = { viewModel.clearCache() },
                onSyncTime = { viewModel.syncTime() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showUpdateConfirm) {
        SecureConfirmGate(
            visible = true,
            onDismiss = { showUpdateConfirm = false },
            onConfirm = { viewModel.runUpdates() },
            title = "Apply system updates?",
            message = "This runs package updates on the host and can take several minutes.",
            details = buildList {
                if (uiState.updateManager.isNotBlank()) add("Manager: ${uiState.updateManager}")
                add("${uiState.availableUpdates.size} package(s)")
            },
            confirmText = "Update",
            icon = Icons.Default.SystemUpdate,
            isDanger = false,
            biometricTitle = "Apply system updates",
            biometricSubtitle = "Confirm package updates on the host"
        )
    }
}

@Composable
private fun MaintenanceSection(title: String, content: @Composable () -> Unit) {
    Column {
        SectionHeader(title)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun UpdateCard(
    manager: String,
    packages: List<VelaPackageUpdate>,
    isUpdating: Boolean,
    warningColor: Color,
    modifier: Modifier = Modifier,
    onRunUpdate: () -> Unit
) {
    Surface(
        color = warningColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, warningColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = warningColor)
                Spacer(modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${packages.size} package${if (packages.size == 1) "" else "s"} ready",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (manager.isNotBlank()) {
                        Text(
                            text = manager,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                packages.take(3).forEach { pkg ->
                    Text(
                        text = "• ${pkg.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (packages.size > 3) {
                    Text(
                        "and ${packages.size - 3} more…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            Button(
                onClick = onRunUpdate,
                enabled = !isUpdating,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = warningColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(9.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Text("Apply updates", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: VelaService,
    expanded: Boolean,
    logs: List<String>,
    isLoadingLogs: Boolean,
    successColor: Color,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onRefreshLogs: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (expanded) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            0.5.dp,
            if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (service.isRunning) successColor
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            CircleShape
                        )
                )

                Spacer(modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = service.description.ifBlank {
                            "${service.active}/${service.sub}".trim('/')
                        }.ifBlank { if (service.isRunning) "Running" else "Stopped" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        if (service.isRunning) {
                            ServiceActionButton(Icons.Default.Stop, onClick = onStop, isDanger = true)
                            ServiceActionButton(Icons.Default.RestartAlt, onClick = onRestart)
                        } else {
                            ServiceActionButton(Icons.Default.PlayArrow, onClick = onStart)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        ServiceActionButton(Icons.Default.Refresh, onClick = onRefreshLogs)
                    }

                    Text(
                        text = "${service.active} · ${service.sub}".trim(' ', '·'),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp)
                    ) {
                        when {
                            isLoadingLogs -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            logs.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No log lines",
                                        color = Color.White.copy(alpha = 0.4f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            else -> {
                                Column(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    logs.forEach { log ->
                                        Text(
                                            text = log,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF6FCB72).copy(alpha = 0.85f),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = if (isDanger) {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.error
            )
        } else {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun HostUpkeepBar(
    onClearCache: () -> Unit,
    onSyncTime: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Host upkeep",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UpkeepAction(
                label = "Clear cache",
                icon = Icons.Default.DeleteSweep,
                onClick = onClearCache,
                modifier = Modifier.weight(1f)
            )
            UpkeepAction(
                label = "Sync time",
                icon = Icons.Default.Sync,
                onClick = onSyncTime,
                modifier = Modifier.weight(1f),
                emphasized = true
            )
        }
    }
}

@Composable
private fun UpkeepAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = if (emphasized) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        border = BorderStroke(
            0.5.dp,
            if (emphasized) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun ColorScheme.isLight(): Boolean =
    background.red > 0.5f && background.green > 0.5f && background.blue > 0.5f
