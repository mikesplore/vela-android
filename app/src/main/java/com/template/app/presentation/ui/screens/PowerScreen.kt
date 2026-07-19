package com.template.app.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.ui.components.SecureConfirmGate
import com.template.app.presentation.viewmodel.PowerViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerScreen(
    onBack: () -> Unit,
    viewModel: PowerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    // Local state for UI feedback
    var actionToConfirm by remember { mutableStateOf<PowerActionType?>(null) }
    var showScheduleSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SectionHeader(title = "Power Profile")
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Selector
            PowerProfileSelector(
                currentProfile = state.currentProfile?:"BALANCED",
                onProfileSelect = { viewModel.setPowerProfile(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(title = "System Controls")
            Spacer(modifier = Modifier.height(16.dp))

            // Danger Zone - Shutdown
            PowerActionTile(
                title = "Shutdown",
                subtitle = "Power off the remote host completely",
                icon = Icons.Default.PowerSettingsNew,
                containerColor = colorScheme.errorContainer,
                contentColor = colorScheme.onErrorContainer,
                onClick = { actionToConfirm = PowerActionType.SHUTDOWN }
            )

            // Warning Zone - Restart
            PowerActionTile(
                title = "Restart",
                subtitle = "Reboot system to clear memory",
                icon = Icons.Default.Refresh,
                containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                contentColor = colorScheme.onTertiaryContainer,
                onClick = { actionToConfirm = PowerActionType.RESTART }
            )

            // Secondary Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallPowerTile(
                    modifier = Modifier.weight(1f),
                    title = "Sleep",
                    icon = Icons.Default.NightsStay,
                    onClick = { actionToConfirm = PowerActionType.SLEEP }
                )
                SmallPowerTile(
                    modifier = Modifier.weight(1f),
                    title = "Hibernate",
                    icon = Icons.Default.AcUnit,
                    onClick = { actionToConfirm = PowerActionType.HIBERNATE }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Automation")
            Spacer(modifier = Modifier.height(16.dp))

            // Schedule Toggle
            PowerActionTile(
                title = "Timed Shutdown",
                subtitle = "Schedule a power-off event",
                icon = Icons.Default.Schedule,
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
                onClick = { showScheduleSheet = true }
            )

            // Abort Button (Static for now or tied to a boolean if you add it to State)
            PowerActionTile(
                title = "Abort Timer",
                subtitle = "Cancel any pending shutdown command",
                icon = Icons.Default.Cancel,
                containerColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.onSurfaceVariant,
                onClick = { viewModel.cancelShutdown() }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    actionToConfirm?.let { action ->
        SecureConfirmGate(
            visible = true,
            onDismiss = { actionToConfirm = null },
            onConfirm = {
                when (action) {
                    PowerActionType.SHUTDOWN -> viewModel.shutdown()
                    PowerActionType.RESTART -> viewModel.restart()
                    PowerActionType.SLEEP -> viewModel.sleep()
                    PowerActionType.HIBERNATE -> viewModel.hibernate()
                }
            },
            title = action.displayName,
            message = "Are you sure you want to perform this action?",
            confirmText = "Confirm",
            isDanger = action == PowerActionType.SHUTDOWN,
            icon = action.icon,
            biometricTitle = action.displayName,
            biometricSubtitle = "Confirm this power action"
        )
    }

    // 2. Schedule Bottom Sheet
    if (showScheduleSheet) {
        ScheduleBottomSheet(
            onDismiss = { showScheduleSheet = false },
            onConfirm = { mins ->
                val at = LocalDateTime.now().plusMinutes(mins)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                viewModel.scheduleShutdown(at)
                showScheduleSheet = false
            }
        )
    }
}

@Composable
fun PowerProfileSelector(
    currentProfile: String,
    onProfileSelect: (String) -> Unit
) {
    val profiles = listOf("performance", "balanced", "power-saver")
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(6.dp)) {
            profiles.forEach { profile ->
                val isSelected = currentProfile == profile
                val animatedColor by animateColorAsState(
                    if (isSelected) colorScheme.primary else Color.Transparent, label = "bg"
                )
                val contentColor by animateColorAsState(
                    if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant, label = "text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(animatedColor)
                        .clickable { onProfileSelect(profile) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.replace("-", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun PowerActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = contentColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun SmallPowerTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBottomSheet(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var minutes by remember { mutableStateOf("60") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text("Schedule Shutdown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = minutes,
                onValueChange = { minutes = it },
                label = { Text("Minutes from now") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Text("min", modifier = Modifier.padding(end = 12.dp)) }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(minutes.toLongOrNull() ?: 60L) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm Schedule")
            }
        }
    }
}

private enum class PowerActionType(val displayName: String, val icon: ImageVector) {
    SHUTDOWN("Shut Down", Icons.Default.PowerSettingsNew),
    RESTART("Restart", Icons.Default.Refresh),
    SLEEP("Sleep", Icons.Default.NightsStay),
    HIBERNATE("Hibernate", Icons.Default.AcUnit)
}