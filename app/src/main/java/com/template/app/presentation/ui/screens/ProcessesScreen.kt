package com.template.app.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.template.app.domain.model.VelaProcess
import com.template.app.presentation.viewmodel.ProcessesSortType
import com.template.app.presentation.viewmodel.ProcessesViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessesScreen(
    onBack: () -> Unit,
    viewModel: ProcessesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme
    var processToKill by remember { mutableStateOf<VelaProcess?>(null) }
    
    val listState = rememberLazyListState()
    
    // Detect when reaching the bottom to load more
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // Load more when user is 2 items away from the bottom
            lastVisibleItemIndex >= totalItemsCount - 2 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search processes...",
                        color = cs.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = cs.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cs.surfaceVariant.copy(alpha = 0.1f),
                    unfocusedContainerColor = cs.surfaceVariant.copy(alpha = 0.1f),
                    focusedBorderColor = cs.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = cs.outlineVariant.copy(alpha = 0.3f),
                    cursorColor = cs.primary,
                    focusedTextColor = cs.onSurface,
                    unfocusedTextColor = cs.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Table Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROCESS",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 0.6.sp
            )
            SortHeader(
                label = "CPU",
                active = state.sortBy == ProcessesSortType.CPU,
                onClick = { viewModel.onSortChanged(ProcessesSortType.CPU) }
            )
            Spacer(Modifier.width(16.dp))
            SortHeader(
                label = "MEM",
                active = state.sortBy == ProcessesSortType.MEM,
                onClick = { viewModel.onSortChanged(ProcessesSortType.MEM) }
            )
            Spacer(Modifier.width(36.dp)) // Alignment offset for the kill button
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 0.5.dp,
            color = cs.outlineVariant.copy(alpha = 0.3f)
        )

        // Process List
        val filteredList = state.processes.filter {
            it.name.contains(state.searchQuery, ignoreCase = true)
        }.let { list ->
            if (state.sortBy == ProcessesSortType.CPU) list.sortedByDescending { it.cpu }
            else list.sortedByDescending { it.mem }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(filteredList, key = { it.pid }) { process ->
                ProcessRow(process, onKill = { processToKill = process })
            }
            
            // Show loading or limit message at the bottom
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (filteredList.size >= state.currentLimit) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else if (filteredList.isNotEmpty()) {
                        Text(
                            text = "End of list (${filteredList.size} processes)",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    if (processToKill != null) {
        val process = processToKill!!
        ModalBottomSheet(
            onDismissRequest = { processToKill = null },
            containerColor = cs.surfaceContainerHigh,
            dragHandle = { BottomSheetDefaults.DragHandle(color = cs.outlineVariant) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(cs.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = cs.error,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Terminate Process?",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to kill \"${process.name}\" (PID ${process.pid})? This may cause system instability or data loss.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { processToKill = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            viewModel.killProcess(process.pid)
                            processToKill = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.error
                        )
                    ) {
                        Text("Terminate", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun SortHeader(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (active) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
        if (active) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ProcessRow(
    process: VelaProcess,
    onKill: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = process.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = String.format(Locale.ROOT, "%.1f%%", process.cpu),
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = cs.primary
        )
        Text(
            text = String.format(Locale.ROOT, "%.1f%%", process.mem),
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = cs.onSurfaceVariant
        )
        IconButton(
            onClick = onKill,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Kill",
                tint = cs.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = cs.outlineVariant.copy(alpha = 0.2f)
    )
}
