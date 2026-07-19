package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.template.app.domain.model.AppThemeMode
import com.template.app.domain.model.PairedDevice
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.VelaConfirmationSheet
import com.template.app.presentation.ui.components.VelaPinSheet
import com.template.app.presentation.ui.components.rememberBiometricAuth
import com.template.app.presentation.ui.components.rememberBiometricAvailable
import com.template.app.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private enum class PinCaptureMode { ENABLE, UPDATE }

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onCredentialsCleared: () -> Unit = {},
    onAddDevice: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var showRemoveAllConfirm by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<PairedDevice?>(null) }
    var renameText by remember { mutableStateOf("") }
    var showDisableBiometricsConfirm by remember { mutableStateOf(false) }
    var showPinCapture by remember { mutableStateOf(false) }
    var pinCaptureMode by remember { mutableStateOf(PinCaptureMode.ENABLE) }
    val biometricAvailable = rememberBiometricAvailable()
    val biometricAuth = rememberBiometricAuth()
    val snackbarHostState = remember { SnackbarHostState() }

    fun startEnableBiometrics() {
        if (!biometricAvailable || biometricAuth == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Biometrics are not available on this device")
            }
            return
        }
        biometricAuth.authenticate(
            title = "Enable biometrics",
            subtitle = "Verify your identity to store an agent PIN",
            onSuccess = {
                pinCaptureMode = PinCaptureMode.ENABLE
                showPinCapture = true
            },
            onErrorOrCancel = { }
        )
    }

    fun startUpdatePin() {
        if (biometricAuth == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Biometrics are not available on this device")
            }
            return
        }
        biometricAuth.authenticate(
            title = "Update agent PIN",
            subtitle = "Verify your identity to change the stored PIN",
            onSuccess = {
                pinCaptureMode = PinCaptureMode.UPDATE
                showPinCapture = true
            },
            onErrorOrCancel = { }
        )
    }

    if (state.renameTargetId != null) {
        val target = state.pairedDevices.find { it.id == state.renameTargetId }
        LaunchedEffect(state.renameTargetId) {
            renameText = target?.label.orEmpty()
        }
        AlertDialog(
            onDismissRequest = { viewModel.setRenameTarget(null) },
            title = { Text("Rename device") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Label") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.renameTargetId?.let { viewModel.renameDevice(it, renameText) }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setRenameTarget(null) }) { Text("Cancel") }
            }
        )
    }

    if (showRemoveAllConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveAllConfirm = false },
            title = { Text("Remove all devices?") },
            text = { Text("This clears all paired devices and local cache. You will need to pair again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveAllConfirm = false
                        viewModel.removeAllDevices(onCredentialsCleared)
                    }
                ) { Text("Remove all") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAllConfirm = false }) { Text("Cancel") }
            }
        )
    }

    removeTarget?.let { device ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("Remove ${device.displayName}?") },
            text = { Text("Cached data and chat for this device will be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = device.id
                        removeTarget = null
                        viewModel.removeDevice(id, onCredentialsCleared)
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showDisableBiometricsConfirm) {
        VelaConfirmationSheet(
            onDismiss = { showDisableBiometricsConfirm = false },
            onConfirm = {
                viewModel.disableBiometrics()
                showDisableBiometricsConfirm = false
            },
            title = "Disable biometrics?",
            message = "Stored agent PIN will be removed. Confirm and PIN prompts will use sheets again.",
            confirmText = "Disable",
            isDanger = true,
            icon = Icons.Default.Fingerprint
        )
    }

    if (showPinCapture) {
        VelaPinSheet(
            onDismiss = { showPinCapture = false },
            onSubmit = { pin ->
                when (pinCaptureMode) {
                    PinCaptureMode.ENABLE -> viewModel.enableBiometrics(pin)
                    PinCaptureMode.UPDATE -> viewModel.updateBiometricPin(pin)
                }
                showPinCapture = false
            },
            title = if (pinCaptureMode == PinCaptureMode.ENABLE) {
                "Agent PIN for biometric unlock"
            } else {
                "Update agent PIN"
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        SectionHeader(title = "DEVICES")
        Spacer(modifier = Modifier.height(4.dp))

        state.pairedDevices.forEach { device ->
            DeviceRow(
                device = device,
                onSwitch = { viewModel.switchDevice(device.id) },
                onRename = { viewModel.setRenameTarget(device.id) },
                onRemove = { removeTarget = device }
            )
        }

        TextButton(
            onClick = onAddDevice,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add device", fontSize = 13.sp)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SectionHeader(title = "HOST INFORMATION")
        Spacer(modifier = Modifier.height(8.dp))

        state.device?.let { device ->
            AboutRow(label = "Hostname", value = device.prettyHostname ?: "Unknown")
            AboutRow(label = "OS Distro", value = "${device.osDistro} ${device.osDistroVersion ?: ""}".trim())
            AboutRow(label = "Kernel", value = device.kernel ?: "Unknown")
            AboutRow(label = "Hardware", value = "${device.hardwareVendor} ${device.laptopModel ?: ""}".trim())
            AboutRow(label = "Architecture", value = device.architecture ?: "Unknown")
        } ?: run {
            Text(
                "Loading host information...",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SectionHeader(title = "APPEARANCE")
        Spacer(modifier = Modifier.height(8.dp))

        Column {
            ThemeOption(
                label = "Light Mode",
                isActive = state.themeMode == AppThemeMode.LIGHT,
                onClick = { viewModel.updateTheme(AppThemeMode.LIGHT) }
            )
            ThemeOption(
                label = "Dark Mode",
                isActive = state.themeMode == AppThemeMode.DARK,
                onClick = { viewModel.updateTheme(AppThemeMode.DARK) }
            )
            ThemeOption(
                label = "System Default",
                isActive = state.themeMode == AppThemeMode.SYSTEM,
                onClick = { viewModel.updateTheme(AppThemeMode.SYSTEM) }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SectionHeader(title = "SECURITY")
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Biometric unlock",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (!biometricAvailable) {
                        "No biometrics enrolled on this device"
                    } else {
                        "Use fingerprint or face for confirms and chat PIN"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Switch(
                checked = state.biometricsEnabled,
                enabled = biometricAvailable || state.biometricsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        startEnableBiometrics()
                    } else {
                        showDisableBiometricsConfirm = true
                    }
                }
            )
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        if (state.biometricsEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { startUpdatePin() })
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Password,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Update agent PIN",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SectionHeader(title = "ABOUT")
        AboutRow(label = "App version", value = "1.4.2")
        AboutRow(label = "Agent version", value = state.agentVersion)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SectionHeader(title = "SESSION")
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            onClick = { showRemoveAllConfirm = true },
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Remove all devices",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }
}

@Composable
private fun DeviceRow(
    device: PairedDevice,
    onSwitch: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !device.isActive, onClick = onSwitch)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    device.displayName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                if (device.isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                device.hostname?.takeIf { it.isNotBlank() } ?: device.relayBaseUrl,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(
            onClick = onRename,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Rename",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
            )
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun ThemeOption(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isActive,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
