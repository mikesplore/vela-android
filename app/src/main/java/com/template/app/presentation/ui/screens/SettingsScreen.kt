package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onCredentialsCleared: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // --- HOST INFORMATION SECTION ---
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

        // --- APPEARANCE SECTION ---
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

        // --- ABOUT SECTION ---
        SectionHeader(title = "ABOUT")
        AboutRow(label = "App version", value = "1.4.2")
        AboutRow(label = "Agent version", value = state.agentVersion)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 22.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // --- SESSION SECTION ---
        SectionHeader(title = "SESSION")
        Spacer(modifier = Modifier.height(14.dp))
        
        Surface(
            onClick = { viewModel.clearCredentials(onCredentialsCleared) },
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Logout",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
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
