package com.template.app.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.PowerViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class PowerAction(
    val label: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val confirmLabel: String
) {
    SHUTDOWN(
        "Shutdown", Icons.Default.PowerSettingsNew,
        "Shut down host?", "This will power off the remote machine. You won't be able to reconnect until it's turned back on manually.", "Shut down"
    ),
    RESTART(
        "Restart", Icons.Default.Refresh,
        "Restart host?", "This will reboot the remote machine. Any unsaved data might be lost.", "Restart"
    ),
    SLEEP(
        "Sleep", Icons.Default.NightsStay,
        "Put host to sleep?", "The machine will enter a low-power state. You can wake it up later.", "Sleep"
    ),
    HIBERNATE(
        "Hibernate", Icons.Default.AcUnit,
        "Hibernate host?", "The machine will save its state to disk and power off completely.", "Hibernate"
    ),
    SCHEDULE(
        "Schedule", Icons.Default.Schedule,
        "Schedule shutdown?", "Pick a time to automatically shut down the host.", "Schedule"
    ),
    CANCEL_SCHEDULE(
        "Abort", Icons.Default.Cancel,
        "Cancel shutdown?", "This will abort any currently scheduled shutdown or restart process.", "Abort"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerScreen(
    onBack: () -> Unit,
    viewModel: PowerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmingAction by remember { mutableStateOf<PowerAction?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Power Profile Section
            Text(
                text = "POWER PROFILE",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(0.5.dp, colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val profiles = listOf("performance", "balanced", "power-saver")
                    profiles.forEach { profile ->
                        val isSelected = state.currentProfile == profile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setPowerProfile(profile) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.replace("-", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Power Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    PowerButton(
                        icon = PowerAction.SHUTDOWN.icon,
                        label = PowerAction.SHUTDOWN.label,
                        iconColor = colorScheme.error,
                        onClick = { confirmingAction = PowerAction.SHUTDOWN }
                    )
                }
                item {
                    PowerButton(
                        icon = PowerAction.RESTART.icon,
                        label = PowerAction.RESTART.label,
                        iconColor = colorScheme.tertiary,
                        onClick = { confirmingAction = PowerAction.RESTART }
                    )
                }
                item {
                    PowerButton(
                        icon = PowerAction.SLEEP.icon,
                        label = PowerAction.SLEEP.label,
                        iconColor = colorScheme.secondary,
                        onClick = { confirmingAction = PowerAction.SLEEP }
                    )
                }
                item {
                    PowerButton(
                        icon = PowerAction.HIBERNATE.icon,
                        label = PowerAction.HIBERNATE.label,
                        iconColor = colorScheme.secondary,
                        onClick = { confirmingAction = PowerAction.HIBERNATE }
                    )
                }
                item {
                    PowerButton(
                        icon = PowerAction.SCHEDULE.icon,
                        label = PowerAction.SCHEDULE.label,
                        iconColor = colorScheme.primary,
                        onClick = { confirmingAction = PowerAction.SCHEDULE }
                    )
                }
                item {
                    PowerButton(
                        icon = PowerAction.CANCEL_SCHEDULE.icon,
                        label = PowerAction.CANCEL_SCHEDULE.label,
                        iconColor = colorScheme.error,
                        onClick = { confirmingAction = PowerAction.CANCEL_SCHEDULE }
                    )
                }
            }
        }

        if (confirmingAction != null) {
            val action = confirmingAction!!
            
            ModalBottomSheet(
                onDismissRequest = { confirmingAction = null },
                containerColor = colorScheme.surfaceContainerHigh,
                dragHandle = { BottomSheetDefaults.DragHandle(color = colorScheme.outlineVariant) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp, top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val actionColor = when (action) {
                        PowerAction.SHUTDOWN -> colorScheme.error
                        PowerAction.RESTART -> colorScheme.tertiary
                        PowerAction.CANCEL_SCHEDULE -> colorScheme.error
                        else -> colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(actionColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(action.icon, contentDescription = null, tint = actionColor, modifier = Modifier.size(28.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = action.title, style = MaterialTheme.typography.titleMedium)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = action.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    if (action == PowerAction.SCHEDULE) {
                        var scheduleTime by remember { mutableStateOf("60") } // Default 60 mins
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedTextField(
                            value = scheduleTime,
                            onValueChange = { scheduleTime = it },
                            label = { Text("Minutes from now") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = {
                                val mins = scheduleTime.toLongOrNull() ?: 60L
                                val at = LocalDateTime.now().plusMinutes(mins)
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                viewModel.scheduleShutdown(at)
                                confirmingAction = null
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Schedule Shutdown")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { confirmingAction = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    when (action) {
                                        PowerAction.SHUTDOWN -> viewModel.shutdown()
                                        PowerAction.RESTART -> viewModel.restart()
                                        PowerAction.SLEEP -> viewModel.sleep()
                                        PowerAction.HIBERNATE -> viewModel.hibernate()
                                        PowerAction.CANCEL_SCHEDULE -> viewModel.cancelShutdown()
                                        else -> {}
                                    }
                                    confirmingAction = null
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (action == PowerAction.SHUTDOWN || action == PowerAction.CANCEL_SCHEDULE) colorScheme.error else colorScheme.primary
                                )
                            ) {
                                Text(action.confirmLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCard(message: String, icon: ImageVector, color: Color, onDismiss: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onDismiss() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PowerButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
