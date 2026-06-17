package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.template.app.domain.model.VelaScheduledTask
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.SchedulerState
import com.template.app.presentation.viewmodel.SchedulerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    viewModel: SchedulerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(20.dp)
        ) {
            SectionHeader("Scheduled tasks")
            Spacer(modifier = Modifier.height(14.dp))

            if (state.tasks.isEmpty() && !state.isLoading) {
                EmptyTasksView()
            } else {
                state.tasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onRunNow = { viewModel.runTaskNow(task.id) },
                        onCancel = { viewModel.cancelTask(task.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    if (showAddSheet) {
        CreateTaskSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = { showAddSheet = false },
            onCommandChange = viewModel::updateCommand,
            onToggleRecurring = { viewModel.toggleRecurring(it) },
            onCreate = {
                viewModel.createTask()
                showAddSheet = false
            },
            onDateTimeSelected = { viewModel.updateRunAt(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    state: SchedulerState,
    onDismiss: () -> Unit,
    viewModel: SchedulerViewModel,
    onCommandChange: (String) -> Unit,
    onDateTimeSelected: (String) -> Unit,
    onToggleRecurring: (Boolean) -> Unit,
    onCreate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .imePadding()
        ) {
            Text("Schedule New Task", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            SchedulerFormField(
                label = "Command",
                value = state.command,
                onValueChange = onCommandChange,
                placeholder = "e.g. backup.sh",
                icon = Icons.Default.Terminal
            )

            // Date/Time Selection Trigger
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(9.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (state.runAt.isEmpty()) "Select execution time" else state.runAt,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Recurring Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recurring", modifier = Modifier.weight(1f))
                Switch(checked = state.isRecurring, onCheckedChange = onToggleRecurring)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.command.isNotBlank() && state.runAt.isNotBlank()
            ) {
                Text("Create Task")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    // Format as ISO-8601
                    val isoTimestamp = viewModel.formatToIsoTimestamp(date, timePickerState.hour, timePickerState.minute)
                    onDateTimeSelected(isoTimestamp)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
@Composable
fun TaskCard(
    task: VelaScheduledTask,
    onRunNow: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.command,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                task.recurring?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = it,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "Next run: ${task.nextRun}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskActionButton(
                    text = "Run now",
                    icon = Icons.Default.PlayArrow,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    onClick = onRunNow,
                    modifier = Modifier.weight(1f)
                )
                TaskActionButton(
                    text = "Cancel",
                    icon = Icons.Default.Close,
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TaskActionButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = contentColor)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun SchedulerFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(9.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun EmptyTasksView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No scheduled tasks found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}