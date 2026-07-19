package com.template.app.presentation.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.VelaBreadcrumb
import com.template.app.domain.model.VelaDiskUsage
import com.template.app.domain.model.VelaFileInfo
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.SecureConfirmGate
import com.template.app.presentation.ui.components.VelaInputSheet
import com.template.app.presentation.ui.components.rowDivider
import com.template.app.presentation.viewmodel.FilesViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isFabExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<VelaFileInfo?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<VelaFileInfo?>(null) }
    var showZipDialog by remember { mutableStateOf<List<VelaFileInfo>?>(null) }

    val cs = MaterialTheme.colorScheme

    // File Picker for Upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val tempFile = createTempFileFromUri(context, it)
            if (tempFile != null) {
                viewModel.uploadFile(tempFile)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ───────────────────────────────────────────────────────
            Breadcrumbs(
                breadcrumbs = state.breadcrumbs,
                showHidden = state.showHidden,
                onNavigate = { viewModel.navigateTo(it) },
                onBack = { viewModel.navigateUp() },
                onToggleHidden = { viewModel.toggleShowHidden() }
            )

            // ── Search Bar ────────────────────────────────────────────────────
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )
            AnimatedVisibility(
                visible = state.isRefreshing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // ── File List ──
                val filteredFiles = state.files.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true)
                }

                if (filteredFiles.isNotEmpty()) {
                    items(filteredFiles, key = { it.path }) { file ->
                        FileItemRow(
                            file = file,
                            onClick = {
                                if (file.type == "directory") {
                                    viewModel.navigateTo(file.path)
                                } else {
                                    viewModel.openFile(file)
                                }
                            },
                            onRename = { showRenameDialog = file },
                            onDelete = { showDeleteConfirm = file },
                            onZip = { showZipDialog = listOf(file) },
                            onUnzip = { viewModel.unzipFile(file) }
                        )
                    }
                } else if (!state.isLoading && state.error == null) {
                    // ── EMPTY STATE ──
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.6f) // Center it roughly in the list area
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (state.searchQuery.isEmpty()) "This folder is empty" else "No results found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (state.isLoading && state.files.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight(0.7f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (state.isPerformingAction || state.isUploading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (state.isUploading) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Uploading...",
                                        fontSize = 12.sp,
                                        color = colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.error != null && !state.isLoading) {
                    Log.e("FilesScreen", "Error: ${state.error}")
                    item {
                        ErrorMessage(msg = state.error!!)
                    }
                }
            }
        }

        // ── FAB Group ────────────────────────────────────────────────────────
        FilesFabGroup(
            isExpanded = isFabExpanded,
            onToggle = { isFabExpanded = !isFabExpanded },
            onUpload = {
                isFabExpanded = false
                filePickerLauncher.launch("*/*")
            },
            onNewFolder = {
                isFabExpanded = false
                showNewFolderDialog = true
            }
        )

        // ── Dialogs ──────────────────────────────────────────────────────────

        if (showNewFolderDialog) {
            VelaInputSheet(
                title = "New Folder",
                placeholder = "Enter folder name",
                icon = Icons.Default.CreateNewFolder,
                onDismiss = { showNewFolderDialog = false },
                onConfirm = {
                    viewModel.createDirectory(it)
                    showNewFolderDialog = false
                }
            )
        }


        showRenameDialog?.let { file ->
            VelaInputSheet(
                title = "Rename",
                initialValue = file.name,
                placeholder = "New name",
                icon = Icons.Default.Edit,
                onDismiss = { showRenameDialog = null },
                onConfirm = {
                    viewModel.renameFile(file, it)
                    showRenameDialog = null
                }
            )
        }


        showZipDialog?.let { files ->
            VelaInputSheet(
                title = "Create Zip",
                initialValue = if (files.size == 1) "${files[0].name}.zip" else "archive.zip",
                placeholder = "Filename.zip",
                icon = Icons.Default.FolderZip, // Use a zip-related icon
                onDismiss = { showZipDialog = null },
                onConfirm = {
                    viewModel.zipFiles(files, it)
                    showZipDialog = null
                }
            )
        }

        showDeleteConfirm?.let { file ->
            SecureConfirmGate(
                visible = true,
                onDismiss = { showDeleteConfirm = null },
                onConfirm = { viewModel.deleteFile(file) },
                title = "Delete ${if (file.type == "directory") "Folder" else "File"}",
                message = "Are you sure you want to delete '${file.name}'? This action cannot be undone.",
                confirmText = "Delete",
                icon = Icons.Default.DeleteForever,
                isDanger = true,
                biometricTitle = "Delete ${file.name}",
                biometricSubtitle = "Confirm this delete action"
            )
        }
    }
}

@Composable
private fun Breadcrumbs(
    breadcrumbs: List<VelaBreadcrumb>,
    showHidden: Boolean,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onToggleHidden: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (breadcrumbs.isEmpty()) {
                Text(
                    text = "home",
                    fontSize = 13.sp,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate("") }
                )
            } else {
                breadcrumbs.forEachIndexed { index, crumb ->
                    if (index > 0) {
                        Text(
                            text = " / ",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                    val isLast = index == breadcrumbs.size - 1
                    Text(
                        text = if (crumb.name == "/") "home" else crumb.name,
                        fontSize = 13.sp,
                        color = if (isLast) colorScheme.onSurface else colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        ),
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.clickable { onNavigate(crumb.path) }
                    )
                }
            }
        }

        IconButton(onClick = onToggleHidden, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (showHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle Hidden",
                tint = if (showHidden) colorScheme.primary else colorScheme.onSurfaceVariant.copy(
                    alpha = 0.5f
                ),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Removed the background, clip, and extra padding to make it transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ), // Aligns with Breadcrumbs and File Items
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                color = colorScheme.onSurface,
                fontSize = 15.sp
            ),
            // Added cursor color to match theme
            cursorBrush = SolidColor(colorScheme.primary),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search files...",
                        fontSize = 15.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun DiskUsageSection(disks: List<VelaDiskUsage>) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader("Disk Usage")
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.then(rowDivider(colorScheme)))
            disks.forEachIndexed { index, disk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = disk.mountpoint,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format(Locale.ROOT, "%.1f", disk.percent)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (disk.percent > 85) colorScheme.error else if (disk.percent > 60) colorScheme.tertiary else colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((disk.percent / 100.0).toFloat())
                            .fillMaxHeight()
                            .background(
                                if (disk.percent > 85) colorScheme.error else if (disk.percent > 60) colorScheme.tertiary else colorScheme.primary
                            )
                    )
                }

                if (index < disks.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier.then(rowDivider(colorScheme)))
        }
    }
}

@Composable
private fun FileItemRow(
    file: VelaFileInfo,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onZip: () -> Unit,
    onUnzip: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFolder = file.type == "directory"
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isFolder) colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isFolder -> Icons.Default.Folder
                    file.name.endsWith(".zip") -> Icons.Default.Archive
                    file.name.endsWith(".kt") -> Icons.Default.Code
                    file.name.endsWith(".png") || file.name.endsWith(".jpg") -> Icons.Default.Image
                    else -> Icons.AutoMirrored.Filled.InsertDriveFile
                },
                contentDescription = null,
                tint = if (isFolder) colorScheme.primary else colorScheme.onSurfaceVariant.copy(
                    alpha = 0.6f
                ),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 14.sp,
                color = if (file.isHidden) colorScheme.onSurface.copy(alpha = 0.5f) else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(formatDate(file.modified.toLong()))
                    append(" · ")
                    if (isFolder) {
                        append("Directory")
                        if (file.childrenCount != null) {
                            append(" (${file.childrenCount} items)")
                        }
                    } else {
                        append(formatFileSize(file.size))
                    }
                },
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { showMenu = false; onRename() },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Zip") },
                    onClick = { showMenu = false; onZip() },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Archive,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                if (file.name.endsWith(".zip")) {
                    DropdownMenuItem(
                        text = { Text("Unzip") },
                        onClick = { showMenu = false; onUnzip() },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Unarchive,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = colorScheme.error) },
                    onClick = { showMenu = false; onDelete() },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FilesFabGroup(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onUpload: () -> Unit,
    onNewFolder: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniFabItem(
                        label = "Upload file",
                        icon = Icons.Default.Upload,
                        onClick = onUpload
                    )
                    MiniFabItem(
                        label = "New folder",
                        icon = Icons.Default.CreateNewFolder,
                        onClick = onNewFolder
                    )
                }
            }

            FloatingActionButton(
                onClick = onToggle,
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
}

@Composable
private fun MiniFabItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = CircleShape,
            color = colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TextInputDialog(
    title: String,
    initialValue: String = "",
    placeholder: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ErrorMessage(msg: String) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colorScheme.errorContainer.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Text(msg, color = colorScheme.onErrorContainer, fontSize = 14.sp)
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown"
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000)) // Assuming timestamp is in seconds
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format(
        Locale.ROOT,
        "%.1f %s",
        size / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

// BasicTextField helper for cleaner search bar
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = @Composable { it() }
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        decorationBox = decorationBox,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
    )
}

private fun createTempFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex ?: 0) ?: "upload_file"
        returnCursor?.close()

        val tempFile = File(context.cacheDir, fileName)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
