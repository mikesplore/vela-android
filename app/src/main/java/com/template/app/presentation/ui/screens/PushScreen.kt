package com.template.app.presentation.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.PushViewModel

@Composable
fun PushScreen(
    onBack: (() -> Unit)? = null,
    viewModel: PushViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshLocalState()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    text = "Push",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Phone notifications")
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Register this phone with Vela so the host can send you FCM alerts. " +
                    "Requires the same Firebase project as the server.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatusCard(
                title = "Firebase",
                value = if (state.firebaseReady) "Configured" else "Missing google-services.json"
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusCard(
                title = "Notification permission",
                value = if (state.notificationPermissionGranted) "Granted" else "Not granted"
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusCard(
                title = "FCM token",
                value = state.lastTokenPreview ?: "Not registered yet"
            )

            state.statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.needsNotificationPermission()) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isBusy
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.height(0.dp))
                    Text("  Allow notifications")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (viewModel.needsNotificationPermission()) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        viewModel.register()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy
            ) {
                if (state.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = colorScheme.onPrimary
                    )
                } else {
                    Text("Register this device")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = viewModel::unregister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isBusy && state.lastTokenPreview != null
            ) {
                Text("Unregister")
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
